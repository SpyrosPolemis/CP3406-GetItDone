package com.example.cp3406_getitdone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import android.widget.DatePicker
import java.util.Locale
import java.util.Date
import kotlin.text.format
import java.util.Calendar
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.material.icons.filled.Delete


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        setContent {
            SimpleApp()
        }
    }
}


@Composable
fun SimpleApp() {
    var selectedScreen by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Tasks") },
                    selected = selectedScreen == 0,
                    onClick = { selectedScreen = 0 },
                    label = { Text("Tasks") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Goals") },
                    selected = selectedScreen == 1,
                    onClick = { selectedScreen = 1 },
                    label = { Text("Goals") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Focus") },
                    selected = selectedScreen == 2,
                    onClick = { selectedScreen = 2 },
                    label = { Text("Focus") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedScreen) {
                0 -> ShortTermTaskScreen()
                1 -> PlaceholderScreen("Goals & Habits Coming Soon")
                2 -> PlaceholderScreen("Focus Mode Coming Soon")
            }
        }
    }
}

data class Task(
    val title: String,
    val priority: Int,
    val dueDate: Date?,
    val dueTime: Pair<Int, Int>?,
    val notify: Boolean = false,
    val reminderOffsetMinutes: Int = 30 // default to 30 minutes before
)


@Composable
fun ShortTermTaskScreen() {
    val taskList = remember { mutableStateListOf<Task>() }
    var newTask by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(1) }
    var notify by remember { mutableStateOf(false) }
    var reminderOffset by remember { mutableStateOf(30) } // minutes

    val calendar = remember { Calendar.getInstance() }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var dueTime by remember { mutableStateOf<Pair<Int, Int>?>(null) } // hour, minute
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Today's Tasks", fontSize = 20.sp)

        Row {
            TextField(
                value = newTask,
                onValueChange = { newTask = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("New task") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newTask.isNotBlank()) {
                    val task = Task(newTask, priority, dueDate, dueTime, notify, reminderOffset)
                    taskList.add(task)
                    scheduleReminder(context, task)
                    newTask = ""
                    priority = 1
                    dueDate = null
                    dueTime = null
                    notify = false
                    reminderOffset = 30
                }
            }) {
                Text("Add")
            }
        }

        // Priority Slider
        Column {
            Text("Priority: $priority")
            Slider(
                value = priority.toFloat(),
                onValueChange = { priority = it.toInt() },
                valueRange = 1f..5f,
                steps = 3
            )
        }

        // Pick Due Date
        Button(onClick = {
            val today = calendar
            DatePickerDialog(
                context,
                { view: DatePicker, year: Int, month: Int, dayOfMonth: Int -> // Explicitly type 'view'
                    calendar.set(year, month, dayOfMonth)
                    dueDate = calendar.time
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text(
                if (dueDate != null)
                    "Due Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDate!!)}"
                else
                    "Pick Due Date"
            )
        }

        // Pick Due Time
        Button(onClick = {
            val now = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    dueTime = hourOfDay to minute
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        }) {
            Text(
                if (dueTime != null)
                    "Due Time: %02d:%02d".format(dueTime!!.first, dueTime!!.second)
                else
                    "Pick Due Time"
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notify me", modifier = Modifier.weight(1f))
            Switch(checked = notify, onCheckedChange = { notify = it })
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Reminder time before due:")
        val offsetOptions = listOf(5, 10, 30, 60, 180, 1440) // up to 24 hours (in mins)
        var expanded by remember { mutableStateOf(false) }

        Box {
            Button(onClick = { expanded = true }) {
                Text("$reminderOffset minutes before")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                offsetOptions.forEach { minutes ->
                    DropdownMenuItem(onClick = {
                        reminderOffset = minutes
                        expanded = false
                    }, text = { Text("$minutes minutes before") })
                }
            }
        }


        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            taskList.forEachIndexed { index, task ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dateText = task.dueDate?.let {
                        SimpleDateFormat("dd/MM", Locale.getDefault()).format(it)
                    } ?: "No date"

                    val timeText = task.dueTime?.let {
                        "%02d:%02d".format(it.first, it.second)
                    } ?: "No time"

                    Text(
                        "${task.title} (P${task.priority}, $dateText $timeText)",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { taskList.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

fun scheduleReminder(context: Context, task: Task) {
    if (!task.notify) return // User selects notification or not

    val date = task.dueDate ?: return
    val (hour, minute) = task.dueTime ?: return

    val cal = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }

    cal.add(Calendar.MINUTE, -task.reminderOffsetMinutes) // User selects offset

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", task.title)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        cal.timeInMillis.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    try {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        Toast.makeText(context, "Exact alarms not permitted", Toast.LENGTH_SHORT).show()
    }
}



@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SimpleApp()
}

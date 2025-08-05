@file:OptIn(ExperimentalMaterial3Api::class)

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
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var goalViewModel: GoalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        db = AppDatabase.getDatabase(applicationContext)
        taskDao = db.taskDao()

        val repository = TaskRepository(taskDao)
        val factory = TaskViewModelFactory(repository)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        val goalRepository = GoalRepository(db.goalDao())
        val habitRepository = HabitRepository(db.habitCompletionDao())
        val goalFactory = GoalViewModelFactory(goalRepository, habitRepository)
        goalViewModel = ViewModelProvider(this, goalFactory)[GoalViewModel::class.java]

        setContent {
            SimpleApp(taskViewModel, goalViewModel) // pass the ViewModel here instead of DAO
        }
    }
}

@Composable
fun SimpleApp(taskViewModel: TaskViewModel, goalViewModel: GoalViewModel) {
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
                0 -> ShortTermTaskScreen(taskViewModel)
                1 -> GoalScreen(goalViewModel)
                2 -> FocusScreen()
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
@OptIn(ExperimentalMaterial3Api::class)
fun ShortTermTaskScreen(taskViewModel: TaskViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val taskListState by taskViewModel.allTasks.collectAsState(initial = emptyList())

    var newTask by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(1) }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var dueTime by remember { mutableStateOf<Pair<Int, Int>?>(null) } // hour, minute
    var notify by remember { mutableStateOf(false) }
    var reminderOffset by remember { mutableStateOf(30) }
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showBottomSheet = false
                }
            },
            sheetState = sheetState
        ) {
            TaskInputForm(
                newTask = newTask,
                onTaskChange = { newTask = it },
                priority = priority,
                onPriorityChange = { priority = it },
                dueDate = dueDate,
                onDateChange = { dueDate = it },
                dueTime = dueTime,
                onTimeChange = { dueTime = it },
                notify = notify,
                onNotifyChange = { notify = it },
                reminderOffset = reminderOffset,
                onReminderOffsetChange = { reminderOffset = it },
                onAddClick = {
                    if (newTask.isNotBlank()) {
                        coroutineScope.launch {
                            val entity = TaskEntity(
                                title = newTask,
                                priority = priority,
                                dueDate = dueDate,
                                dueTime = dueTime,
                                notify = notify,
                                reminderOffsetMinutes = reminderOffset
                            )
                            taskViewModel.insertTask(entity)
                            scheduleReminder(context, entity.toTask()) // You will need a conversion function

                            // Reset form
                            newTask = ""
                            priority = 1
                            dueDate = null
                            dueTime = null
                            notify = false
                            reminderOffset = 30

                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showBottomSheet = false
                            }
                        }
                    }
                }
            )
        }
    }

    MainTaskList(
        taskList = taskListState.map { it.toTask() }, // Convert entity to your Task data class
        onDelete = { index ->
            coroutineScope.launch {
                taskViewModel.deleteTask(taskListState[index])
            }
        },
        onFabClick = {
            showBottomSheet = true
            scope.launch { sheetState.show() }
        }
    )
}

@Composable
fun MainTaskList(
    taskList: List<Task>,
    onDelete: (Int) -> Unit,
    onFabClick: () -> Unit
) {
    val now = remember { Calendar.getInstance() }

    val today = now.clone() as Calendar
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val endOfToday = today.clone() as Calendar
    endOfToday.add(Calendar.DAY_OF_YEAR, 1)

    val endOfWeek = today.clone() as Calendar
    endOfWeek.add(Calendar.DAY_OF_YEAR, 7)

    val endOfMonth = today.clone() as Calendar
    endOfMonth.add(Calendar.MONTH, 1)

    val (dueToday, dueThisWeek, dueThisMonth, later) = taskList.sortedBy { it.dueDate?.time ?: Long.MAX_VALUE }
        .partitionByTime(today.time, endOfToday.time, endOfWeek.time, endOfMonth.time)

    Column(modifier = Modifier.padding(16.dp)) {
        HeaderWithAddButton("Today's Tasks", onFabClick)
        TaskSection("Due Today", dueToday, onDelete)
        TaskSection("Due This Week", dueThisWeek, onDelete, startIndex = dueToday.size)
        TaskSection("Due This Month", dueThisMonth, onDelete, startIndex = dueToday.size + dueThisWeek.size)
        TaskSection("Due in a Long Time", later, onDelete, startIndex = dueToday.size + dueThisWeek.size + dueThisMonth.size)
    }
}

@Composable
fun HeaderWithAddButton(title: String, onFabClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title, fontSize = 20.sp)
        IconButton(onClick = onFabClick) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun TaskSection(
    sectionTitle: String,
    tasks: List<Task>,
    onDelete: (Int) -> Unit,
    startIndex: Int = 0
) {
    if (tasks.isNotEmpty()) {
        Text(sectionTitle, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tasks.forEachIndexed { index, task ->
                TaskCard(task = task, onDelete = { onDelete(startIndex + index) })
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateText = task.dueDate?.let {
                SimpleDateFormat("dd/MM", Locale.getDefault()).format(it)
            } ?: "No date"

            val timeText = task.dueTime?.let {
                "%02d:%02d".format(it.first, it.second)
            } ?: "No time"

            val notifText = if (task.notify) "🔔 ${task.reminderOffsetMinutes} min" else ""

            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text("P${task.priority} • $dateText $timeText $notifText", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

fun List<Task>.partitionByTime(
    today: Date,
    endOfToday: Date,
    endOfWeek: Date,
    endOfMonth: Date
): Quadruple<List<Task>, List<Task>, List<Task>, List<Task>> {
    val todayList = mutableListOf<Task>()
    val weekList = mutableListOf<Task>()
    val monthList = mutableListOf<Task>()
    val laterList = mutableListOf<Task>()

    for (task in this) {
        val date = task.dueDate
        if (date == null) {
            laterList.add(task)
        } else if (date.before(endOfToday)) {
            todayList.add(task)
        } else if (date.before(endOfWeek)) {
            weekList.add(task)
        } else if (date.before(endOfMonth)) {
            monthList.add(task)
        } else {
            laterList.add(task)
        }
    }

    return Quadruple(todayList, weekList, monthList, laterList)
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

fun TaskEntity.toTask() = Task(
    title = this.title,
    priority = this.priority,
    dueDate = this.dueDate,
    dueTime = this.dueTime,
    notify = this.notify,
    reminderOffsetMinutes = this.reminderOffsetMinutes
)

@Composable
fun TaskInputForm(
    newTask: String,
    onTaskChange: (String) -> Unit,
    priority: Int,
    onPriorityChange: (Int) -> Unit,
    dueDate: Date?,
    onDateChange: (Date?) -> Unit,
    dueTime: Pair<Int, Int>?,
    onTimeChange: (Pair<Int, Int>?) -> Unit,
    notify: Boolean,
    onNotifyChange: (Boolean) -> Unit,
    reminderOffset: Int,
    onReminderOffsetChange: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = newTask,
            onValueChange = onTaskChange,
            placeholder = { Text("New task") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Priority: $priority")
        Slider(
            value = priority.toFloat(),
            onValueChange = { onPriorityChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3
        )

        Button(onClick = {
            val today = calendar
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    onDateChange(calendar.time)
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text(
                if (dueDate != null)
                    "Due Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDate)}"
                else
                    "Pick Due Date"
            )
        }

        Button(onClick = {
            val now = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onTimeChange(hour to minute)
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        }) {
            Text(
                if (dueTime != null)
                    "Due Time: %02d:%02d".format(dueTime.first, dueTime.second)
                else
                    "Pick Due Time"
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notify me", modifier = Modifier.weight(1f))
            Switch(checked = notify, onCheckedChange = onNotifyChange)
        }

        Text("Reminder time before due:")
        val offsetOptions = listOf(5, 10, 30, 60, 180, 1440)
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
                    DropdownMenuItem(
                        onClick = {
                            onReminderOffsetChange(minutes)
                            expanded = false
                        },
                        text = { Text("$minutes minutes before") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
            Text("Add Task")
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

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(goalViewModel: GoalViewModel) {
    val goals by goalViewModel.allGoals.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showInputSheet by remember { mutableStateOf(false) }

    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    // Input Sheet
    if (showInputSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                    showInputSheet = false
                }
            },
            sheetState = sheetState
        ) {
            GoalInputForm(
                onAddGoal = { goal ->
                    goalViewModel.addGoal(goal)
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showInputSheet = false
                    }
                }
            )
        }
    }

    // Detail Sheet
    if (showDetailSheet && selectedGoal != null) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch { detailSheetState.hide() }.invokeOnCompletion {
                    showDetailSheet = false
                    selectedGoal = null
                }
            },
            sheetState = detailSheetState
        ) {
            GoalDetailSheet(goal = selectedGoal!!, goalViewModel = goalViewModel)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Goals", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = {
                    showInputSheet = true
                    coroutineScope.launch { sheetState.show() }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val goalCompletionsMap by goalViewModel.weeklyCompletions.collectAsState()

            LazyColumn {
                items(goals) { goal ->
                    val completionsThisWeek = goalCompletionsMap[goal.id] ?: 0
                    GoalCard(
                        goal = goal,
                        completionsThisWeek = completionsThisWeek,
                        onClick = {
                            selectedGoal = goal
                            showDetailSheet = true
                            coroutineScope.launch { detailSheetState.show() }
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    completionsThisWeek: Int,
    onClick: () -> Unit
) {
    val progress = (completionsThisWeek.toFloat() / goal.habitFrequencyPerWeek.toFloat()).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(goal.goaldueDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.goalTitle, style = MaterialTheme.typography.titleMedium)
                Text("Due: $formattedDate", style = MaterialTheme.typography.bodySmall)
                if (goal.habitDescription.isNotEmpty()) {
                    Text(goal.habitDescription, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .padding(start = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    strokeWidth = 5.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = 3.dp) // Marker please ignore this, this damn circle was stuck to the screen
                )
                Text(
                    "$percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}


@Composable
fun GoalInputForm(
    onAddGoal: (GoalEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var habitDesc by remember { mutableStateOf("") }
    var freqPerWeek by remember { mutableStateOf(3) }
    var reason by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title - What do you want to do?") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            val today = calendar
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
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

        OutlinedTextField(
            value = habitDesc,
            onValueChange = { habitDesc = it },
            label = { Text("Habit - How are you going to achieve it?") },
            modifier = Modifier.fillMaxWidth()
        )


        Text("Times per week: $freqPerWeek")
        Slider(
            value = freqPerWeek.toFloat(),
            onValueChange = { freqPerWeek = it.toInt() },
            valueRange = 1f..7f,
            steps = 5
        )

        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason - Why do you want to do this?") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    val goal = GoalEntity(
                        goalTitle = title,
                        goaldueDate = dueDate ?: Date(),
                        habitDescription = habitDesc,
                        habitFrequencyPerWeek = freqPerWeek,
                        goalReason = reason
                    )
                    onAddGoal(goal)

                    // Reset fields
                    title = ""
                    dueDate = null
                    habitDesc = ""
                    freqPerWeek = 3
                    reason = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Goal")
        }
    }
}

@Composable
fun GoalDetailSheet(goal: GoalEntity, goalViewModel: GoalViewModel) {
    val completions = goalViewModel.getCompletionsThisWeek(goal.id).collectAsState()
    val totalNeeded = goal.habitFrequencyPerWeek
    val progress = (completions.value.toFloat() / totalNeeded.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(goal.goalTitle, style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        Text("Due Date", style = MaterialTheme.typography.labelMedium)
        Text(goal.goaldueDate.toString(), style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(8.dp))

        Text("Habit", style = MaterialTheme.typography.labelMedium)
        Text(goal.habitDescription, style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(8.dp))

        Text("Times per week: ${goal.habitFrequencyPerWeek}", style = MaterialTheme.typography.labelMedium)

        Spacer(Modifier.height(16.dp))

        Text("Progress this week", style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("${completions.value} / $totalNeeded completed")

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                goalViewModel.markHabitDone(goal.id)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mark as Complete")
        }

        Spacer(Modifier.height(16.dp))

        Text("Why this goal?", style = MaterialTheme.typography.labelMedium)
        Text(goal.goalReason, style = MaterialTheme.typography.bodyLarge)
    }
}


@Composable
fun FocusScreen() {
    var timeLeft by remember { mutableStateOf(600) } // 10 minutes
    var isFocusing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(isFocusing) {
        if (isFocusing) {
            val timerJob = CoroutineScope(Dispatchers.Main).launch {
                while (timeLeft > 0) {
                    delay(1000L)
                    timeLeft--
                }
                isFocusing = false
            }

            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP && timeLeft > 0) {
                    // Consequence: play tone if user leaves app
                    val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                }
            }

            ProcessLifecycleOwner.get().lifecycle.addObserver(observer)

            onDispose {
                timerJob.cancel()
                ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
            }
        } else {
            onDispose { }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Time left: ${timeLeft}s")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            isFocusing = true
            timeLeft = 600
        }) {
            Text("Start Focus")
        }
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

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    SimpleApp()
//}

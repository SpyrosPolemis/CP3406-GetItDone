package com.example.cp3406_getitdone.ui.components.tasks

import androidx.compose.runtime.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                    "Due Date: ${
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(dueDate)
                    }"
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
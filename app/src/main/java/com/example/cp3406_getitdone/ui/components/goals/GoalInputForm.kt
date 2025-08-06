package com.example.cp3406_getitdone.ui.components.goals

import androidx.compose.runtime.*
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cp3406_getitdone.data.local.entity.GoalEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                    "Due Date: ${
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(dueDate!!)
                    }"
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
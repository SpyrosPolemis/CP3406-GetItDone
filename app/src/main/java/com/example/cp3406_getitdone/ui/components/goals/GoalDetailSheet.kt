package com.example.cp3406_getitdone.ui.components.goals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cp3406_getitdone.data.local.entity.GoalEntity
import com.example.cp3406_getitdone.presentation.GoalViewModel

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

        Text(
            "Times per week: ${goal.habitFrequencyPerWeek}",
            style = MaterialTheme.typography.labelMedium
        )

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
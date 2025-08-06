package com.example.cp3406_getitdone.ui.components.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cp3406_getitdone.data.local.entity.GoalEntity
import java.text.SimpleDateFormat
import java.util.Locale

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
                        .offset(y = 3.dp)
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
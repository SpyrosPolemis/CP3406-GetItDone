package com.example.cp3406_getitdone.ui.components.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cp3406_getitdone.domain.Task
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.graphics.Color

val priorityColors = listOf(
    Color(0xFFEDE7F6), // Priority 1 - light lavender
    Color(0xFFD1C4E9), // Priority 2
    Color(0xFFB39DDB), // Priority 3
    Color(0xFF9575CD), // Priority 4
    Color(0xFF673AB7), // Priority 5 - deep purple
)

@Composable
fun TaskCard(task: Task, onDelete: () -> Unit) {
    val bgColor = priorityColors.getOrElse(task.priority - 1) { Color.LightGray }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
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
                Text(
                    "P${task.priority} • $dateText $timeText $notifText",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
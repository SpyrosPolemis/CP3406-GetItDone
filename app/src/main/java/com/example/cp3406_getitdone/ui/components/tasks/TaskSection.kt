package com.example.cp3406_getitdone.ui.components.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cp3406_getitdone.domain.Task

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
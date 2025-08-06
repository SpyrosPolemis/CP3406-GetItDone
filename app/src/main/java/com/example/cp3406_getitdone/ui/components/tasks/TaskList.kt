package com.example.cp3406_getitdone.ui.components.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cp3406_getitdone.domain.Task
import com.example.cp3406_getitdone.domain.partitionByTime
import com.example.cp3406_getitdone.ui.components.PageHeader
import java.util.Calendar

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
        .partitionByTime(endOfToday.time, endOfWeek.time, endOfMonth.time)

    Column(modifier = Modifier.padding(16.dp)) {
        PageHeader("Tasks", onFabClick)
        TaskSection("Due Today", dueToday, onDelete)
        TaskSection("Due This Week", dueThisWeek, onDelete, startIndex = dueToday.size)
        TaskSection(
            "Due This Month",
            dueThisMonth,
            onDelete,
            startIndex = dueToday.size + dueThisWeek.size
        )
        TaskSection(
            "Due in a Long Time",
            later,
            onDelete,
            startIndex = dueToday.size + dueThisWeek.size + dueThisMonth.size
        )
    }
}


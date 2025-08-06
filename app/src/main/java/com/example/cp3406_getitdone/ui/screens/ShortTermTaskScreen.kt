package com.example.cp3406_getitdone.ui.screens

import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.cp3406_getitdone.data.local.entity.TaskEntity
import com.example.cp3406_getitdone.domain.Task
import com.example.cp3406_getitdone.ui.components.tasks.MainTaskList
import com.example.cp3406_getitdone.ui.components.tasks.TaskInputForm
import com.example.cp3406_getitdone.presentation.TaskViewModel
import com.example.cp3406_getitdone.util.scheduleReminder
import kotlinx.coroutines.launch
import java.util.Date

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
                            scheduleReminder(context, entity.toTask())

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
        taskList = taskListState.map { it.toTask() },
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

fun TaskEntity.toTask() = Task(
    title = this.title,
    priority = this.priority,
    dueDate = this.dueDate,
    dueTime = this.dueTime,
    notify = this.notify,
    reminderOffsetMinutes = this.reminderOffsetMinutes
)

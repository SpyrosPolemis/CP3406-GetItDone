package com.example.cp3406_getitdone.presentation

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.cp3406_getitdone.ui.screens.FocusScreen
import com.example.cp3406_getitdone.ui.screens.GoalScreen
import com.example.cp3406_getitdone.ui.screens.ShortTermTaskScreen

@Composable
fun GetItDone(taskViewModel: TaskViewModel, goalViewModel: GoalViewModel) {
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
package com.example.cp3406_getitdone.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cp3406_getitdone.data.local.entity.GoalEntity
import com.example.cp3406_getitdone.ui.components.goals.GoalCard
import com.example.cp3406_getitdone.ui.components.goals.GoalDetailSheet
import com.example.cp3406_getitdone.ui.components.goals.GoalInputForm
import com.example.cp3406_getitdone.presentation.GoalViewModel
import com.example.cp3406_getitdone.ui.components.PageHeader
import kotlinx.coroutines.launch

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

    // Goal Details Screen
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

            PageHeader("Goals") {
                showInputSheet = true
                coroutineScope.launch { sheetState.show() }
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
                        },
                        onDelete = {
                            goalViewModel.deleteGoal(goal)
                        }
                    )
                }
            }

        }
    }
}
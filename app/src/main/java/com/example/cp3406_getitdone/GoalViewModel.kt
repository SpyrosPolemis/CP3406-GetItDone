package com.example.cp3406_getitdone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoalViewModel(private val repository: GoalRepository) : ViewModel() {

    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .map { it.sortedByDescending { goal -> goal.goaldueDate } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGoal = MutableStateFlow<GoalEntity?>(null)
    val selectedGoal = _selectedGoal.asStateFlow()

    private val _weeklyCompletions = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val weeklyCompletions: StateFlow<Map<Int, Int>> = _weeklyCompletions

    fun getCompletionsThisWeek(goalId: Int): StateFlow<Int> {
        return weeklyCompletions.map { it[goalId] ?: 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    }

    fun markHabitDone(goalId: Int) {
        // your logic to save completion...
        val current = _weeklyCompletions.value[goalId] ?: 0
        _weeklyCompletions.value = _weeklyCompletions.value.toMutableMap().apply {
            this[goalId] = current + 1
        }
    }

    fun selectGoal(goal: GoalEntity) {
        _selectedGoal.value = goal
    }

    fun clearSelectedGoal() {
        _selectedGoal.value = null
    }

    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.insert(goal)
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.update(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.delete(goal)
        }
    }
}


class GoalViewModelFactory(private val repository: GoalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

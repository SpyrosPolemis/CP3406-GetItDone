package com.example.cp3406_getitdone.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cp3406_getitdone.data.local.entity.HabitCompletion
import com.example.cp3406_getitdone.data.repository.HabitRepository
import com.example.cp3406_getitdone.data.local.entity.GoalEntity
import com.example.cp3406_getitdone.data.repository.GoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

class GoalViewModel(private val repository: GoalRepository, private val habitRepository: HabitRepository) : ViewModel() {

    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .map { it.sortedByDescending { goal -> goal.goaldueDate } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _weeklyCompletions = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val weeklyCompletions: StateFlow<Map<Int, Int>> = _weeklyCompletions

    private var lastLoadedWeek = -1

    fun getCompletionsThisWeek(goalId: Int): StateFlow<Int> {
        return weeklyCompletions.map { it[goalId] ?: 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    }

    fun markHabitDone(goalId: Int) {
        viewModelScope.launch {
            refreshWeeklyCompletions()

            val completion = HabitCompletion(goalId = goalId, date = Date())
            habitRepository.insertHabitCompletion(completion)

            val currentMap = _weeklyCompletions.value.toMutableMap()
            val currentCount = currentMap[goalId] ?: 0
            currentMap[goalId] = currentCount + 1
            _weeklyCompletions.value = currentMap
        }
    }


    private fun getCurrentWeekDates(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val end = calendar.time
        return Pair(start, end)
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

    init {
        refreshWeeklyCompletions()
    }
    fun refreshWeeklyCompletions() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY }
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            if (currentWeek != lastLoadedWeek) {
                val (start, end) = getCurrentWeekDates()
                val goals = repository.allGoals.first()
                val completionsMap = mutableMapOf<Int, Int>()
                goals.forEach { goal ->
                    val count = habitRepository.getCompletionsBetween(goal.id, start, end)
                        .firstOrNull() ?: 0
                    completionsMap[goal.id] = count
                }
                _weeklyCompletions.value = completionsMap
                lastLoadedWeek = currentWeek
            }
        }
    }



}


class GoalViewModelFactory(private val repository: GoalRepository, private val habitRepository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalViewModel(repository, habitRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

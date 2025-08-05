package com.example.cp3406_getitdone

import kotlinx.coroutines.flow.Flow
import java.util.Date

class HabitRepository(private val habitCompletionDao: HabitCompletionDao) {

    suspend fun insertHabitCompletion(completion: HabitCompletion) {
        habitCompletionDao.insert(completion)
    }

    fun getCompletionsBetween(goalId: Int, start: Date, end: Date): Flow<Int> {
        return habitCompletionDao.getCompletionsBetween(goalId, start, end)
    }
}


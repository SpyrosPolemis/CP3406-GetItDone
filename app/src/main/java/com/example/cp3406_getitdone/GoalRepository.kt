package com.example.cp3406_getitdone

import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {

    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    suspend fun insert(goal: GoalEntity) {
        goalDao.insertGoal(goal)
    }

    suspend fun update(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    suspend fun delete(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }
}


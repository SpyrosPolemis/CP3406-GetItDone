package com.example.cp3406_getitdone.data.local.dao

import androidx.room.*
import com.example.cp3406_getitdone.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals ORDER BY goaldueDate ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>
}

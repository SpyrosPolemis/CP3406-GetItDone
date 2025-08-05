package com.example.cp3406_getitdone

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface HabitCompletionDao {

    @Insert
    suspend fun insert(completion: HabitCompletion)

    @Query("SELECT COUNT(*) FROM habit_completions WHERE goalId = :goalId AND date BETWEEN :start AND :end")
    fun getCompletionsBetween(goalId: Int, start: Date, end: Date): Flow<Int>
}

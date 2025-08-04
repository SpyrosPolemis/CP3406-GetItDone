package com.example.cp3406_getitdone

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalTitle: String,
    val goaldueDate: Date,
    val habitDescription: String,
    val habitFrequencyPerWeek: Int,
    val goalReason: String
)

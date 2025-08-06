package com.example.cp3406_getitdone.domain

import java.util.Date

data class Task(
    val title: String,
    val priority: Int,
    val dueDate: Date?,
    val dueTime: Pair<Int, Int>?,
    val notify: Boolean = false,
    val reminderOffsetMinutes: Int = 30 // default to 30 minutes before
)
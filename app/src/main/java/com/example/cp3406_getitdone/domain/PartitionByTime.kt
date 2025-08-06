package com.example.cp3406_getitdone.domain

import java.util.Date

fun List<Task>.partitionByTime(
    endOfToday: Date,
    endOfWeek: Date,
    endOfMonth: Date
): Quadruple<List<Task>, List<Task>, List<Task>, List<Task>> {
    val todayList = mutableListOf<Task>()
    val weekList = mutableListOf<Task>()
    val monthList = mutableListOf<Task>()
    val laterList = mutableListOf<Task>()

    for (task in this) {
        val date = task.dueDate
        if (date == null) {
            laterList.add(task)
        } else if (date.before(endOfToday)) {
            todayList.add(task)
        } else if (date.before(endOfWeek)) {
            weekList.add(task)
        } else if (date.before(endOfMonth)) {
            monthList.add(task)
        } else {
            laterList.add(task)
        }
    }

    return Quadruple(todayList, weekList, monthList, laterList)
}
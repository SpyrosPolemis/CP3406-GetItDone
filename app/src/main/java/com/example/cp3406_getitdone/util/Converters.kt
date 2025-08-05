package com.example.cp3406_getitdone.util

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTimePair(value: String?): Pair<Int, Int>? {
        return value?.split(":")?.let {
            if (it.size == 2) Pair(it[0].toInt(), it[1].toInt()) else null
        }
    }

    @TypeConverter
    fun timePairToString(pair: Pair<Int, Int>?): String? {
        return pair?.let { "${it.first}:${it.second}" }
    }
}

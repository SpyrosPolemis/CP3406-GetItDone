package com.example.cp3406_getitdone.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cp3406_getitdone.util.Converters
import com.example.cp3406_getitdone.data.local.dao.GoalDao
import com.example.cp3406_getitdone.data.local.entity.GoalEntity
import com.example.cp3406_getitdone.data.local.entity.HabitCompletion
import com.example.cp3406_getitdone.data.local.dao.HabitCompletionDao
import com.example.cp3406_getitdone.data.local.dao.TaskDao
import com.example.cp3406_getitdone.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class, GoalEntity::class, HabitCompletion::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun goalDao(): GoalDao
    abstract fun habitCompletionDao(): HabitCompletionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "your_database_name"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
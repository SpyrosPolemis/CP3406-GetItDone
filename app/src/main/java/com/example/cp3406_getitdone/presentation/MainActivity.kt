package com.example.cp3406_getitdone.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import com.example.cp3406_getitdone.data.local.dao.TaskDao
import com.example.cp3406_getitdone.data.local.db.AppDatabase
import com.example.cp3406_getitdone.data.repository.GoalRepository
import com.example.cp3406_getitdone.data.repository.HabitRepository
import com.example.cp3406_getitdone.data.repository.TaskRepository


class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var goalViewModel: GoalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        db = AppDatabase.getDatabase(applicationContext)
        taskDao = db.taskDao()

        val repository = TaskRepository(taskDao)
        val factory = TaskViewModelFactory(repository)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        val goalRepository = GoalRepository(db.goalDao())
        val habitRepository = HabitRepository(db.habitCompletionDao())
        val goalFactory = GoalViewModelFactory(goalRepository, habitRepository)
        goalViewModel = ViewModelProvider(this, goalFactory)[GoalViewModel::class.java]

        setContent {
            GetItDone(taskViewModel, goalViewModel)
        }
    }
}



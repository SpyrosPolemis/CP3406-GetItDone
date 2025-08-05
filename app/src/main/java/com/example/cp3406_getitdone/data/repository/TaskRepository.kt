package com.example.cp3406_getitdone.data.repository

import com.example.cp3406_getitdone.data.local.dao.TaskDao
import com.example.cp3406_getitdone.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

}

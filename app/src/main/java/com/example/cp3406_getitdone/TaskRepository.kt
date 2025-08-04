package com.example.cp3406_getitdone

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

}

package com.example.cp3406_getitdone.unit

import androidx.lifecycle.MutableLiveData
import org.junit.Test
import org.junit.Assert.*
import androidx.lifecycle.LiveData

class TaskViewModelTest {

    class FakeTaskRepository {
        private val _tasks = mutableListOf<Task>()
        val tasksLiveData = MutableLiveData<List<Task>>()

        fun getTasks(): LiveData<List<Task>> {
            tasksLiveData.value = _tasks
            return tasksLiveData
        }

        fun addTask(task: Task) {
            _tasks.add(task)
            tasksLiveData.value = _tasks
        }
    }

    class TaskViewModel(private val repository: FakeTaskRepository) {
        val tasks: LiveData<List<Task>> = repository.getTasks()

        fun addTask(title: String) {
            repository.addTask(Task(title = title))
        }
    }

    data class Task(
        val title: String,
        val isCompleted: Boolean = false
    )

    @Test
    fun addTask_increasesListSize() {
        val viewModel = TaskViewModel(FakeTaskRepository())
        val initialSize = viewModel.tasks.value?.size ?: 0
        viewModel.addTask("New Task")
        val newSize = viewModel.tasks.value?.size ?: 0
        assertEquals(initialSize + 1, newSize)
    }
}

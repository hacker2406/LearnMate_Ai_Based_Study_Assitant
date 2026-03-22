package com.example.learnmate.ui.planner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.learnmate.data.model.Task
import com.example.learnmate.data.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class TaskViewModel : ViewModel() {

    private val repository = TaskRepository()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ── Real-time tasks from Firestore ─────────────────────────────────
    val tasks: LiveData<List<Task>> =
        repository.getTasks(userId).asLiveData()

    // ── UI state ───────────────────────────────────────────────────────
    private val _taskState = MutableLiveData<TaskState>(TaskState.Idle)
    val taskState: LiveData<TaskState> = _taskState

    // ── Save task ──────────────────────────────────────────────────────
    fun saveTask(
        title: String,
        description: String,
        dueDate: Long,
        priority: String
    ) {
        if (title.isBlank()) {
            _taskState.value = TaskState.Error("Task title cannot be empty")
            return
        }

        viewModelScope.launch {
            _taskState.value = TaskState.Loading

            val task = Task(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title.trim(),
                description = description.trim(),
                dueDate = dueDate,
                priority = priority,
                isCompleted = false,
                timestamp = System.currentTimeMillis()
            )

            val result = repository.saveTask(task)
            _taskState.value = if (result.isSuccess) {
                TaskState.Success("Task added!")
            } else {
                TaskState.Error(result.exceptionOrNull()?.message ?: "Failed to save")
            }
        }
    }

    // ── Toggle complete ────────────────────────────────────────────────
    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskComplete(userId, task.id, !task.isCompleted)
        }
    }

    // ── Delete task ────────────────────────────────────────────────────
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _taskState.value = TaskState.Loading
            val result = repository.deleteTask(userId, task.id)
            _taskState.value = if (result.isSuccess) {
                TaskState.Success("Task deleted")
            } else {
                TaskState.Error(result.exceptionOrNull()?.message ?: "Failed to delete")
            }
        }
    }
}

// ── State sealed class ─────────────────────────────────────────────────
sealed class TaskState {
    object Idle : TaskState()
    object Loading : TaskState()
    data class Success(val message: String) : TaskState()
    data class Error(val message: String) : TaskState()
}
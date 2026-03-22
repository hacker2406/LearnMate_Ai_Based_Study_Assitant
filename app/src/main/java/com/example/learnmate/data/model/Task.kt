package com.example.learnmate.data.model

import java.io.Serializable

data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Long = 0L,
    val priority: String = "MEDIUM",   // LOW / MEDIUM / HIGH
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

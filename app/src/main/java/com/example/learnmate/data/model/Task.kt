package com.example.learnmate.data.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Long = 0L,
    val priority: String = "MEDIUM",
    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
package com.example.learnmate.data.model

import java.io.Serializable

data class Note(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

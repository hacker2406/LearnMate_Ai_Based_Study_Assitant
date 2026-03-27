package com.example.learnmate.data.model

data class QuizHistory(
    val id: String = "",
    val userId: String = "",
    val topic: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val xpEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

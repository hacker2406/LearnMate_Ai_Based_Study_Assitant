package com.example.learnmate.data.model

data class WeeklyStats(
    val dayLabels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    val studyMinutesPerDay: List<Int> = List(7) { 0 },
    val totalStudyMinutesThisWeek: Int = 0,
    val tasksCompletedThisWeek: Int = 0,
    val quizzesTakenThisWeek: Int = 0,
    val averageQuizScore: Int = 0,
    val totalQuizXP: Int = 0,
    val totalQuizzesTaken: Int = 0
)
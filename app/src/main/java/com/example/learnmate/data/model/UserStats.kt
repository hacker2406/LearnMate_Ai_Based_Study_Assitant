package com.example.learnmate.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties

data class UserStats(
    val userId: String = "",
    val xp: Int = 0,
    val level: Int = 1,
    val levelTitle: String = "Beginner",
    val streak: Int = 0,
    val lastStudyDate: Long = 0L,
    val totalStudyMinutes: Int = 0,
    val todayStudyMinutes: Int = 0,
    val lastStudyDateString: String = ""  // "yyyy-MM-dd" for day comparison
)

package com.example.learnmate.data.repository

import com.example.learnmate.data.model.WeeklyStats
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class ProgressRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun userDoc(userId: String) =
        firestore.collection("users").document(userId)

    // ── Fetch all progress data ────────────────────────────────────────
    suspend fun getWeeklyStats(userId: String): Result<WeeklyStats> {
        return try {
            // Get start of current week (Monday)
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = (dayOfWeek + 5) % 7
            calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
            val weekStart = calendar.timeInMillis

            // Fetch tasks completed this week
            val tasksSnapshot = userDoc(userId)
                .collection("tasks")
                .whereEqualTo("isCompleted", true)
                .get()
                .await()

            val tasksThisWeek = tasksSnapshot.documents.count { doc ->
                val timestamp = doc.getLong("timestamp") ?: 0L
                timestamp >= weekStart
            }

            // Fetch quiz history this week
            val quizSnapshot = userDoc(userId)
                .collection("quizHistory")
                .get()
                .await()

            val allQuizzes = quizSnapshot.documents.mapNotNull { doc ->
                val timestamp = doc.getLong("timestamp") ?: 0L
                val score     = doc.getLong("score")?.toInt() ?: 0
                val total     = doc.getLong("totalQuestions")?.toInt() ?: 1
                val xp        = doc.getLong("xpEarned")?.toInt() ?: 0
                Triple(timestamp, score * 100 / total, xp)
            }

            val quizzesThisWeek  = allQuizzes.count { it.first >= weekStart }
            val avgScore         = if (allQuizzes.isNotEmpty())
                allQuizzes.sumOf { it.second } / allQuizzes.size else 0
            val totalQuizXP      = allQuizzes.sumOf { it.third }
            val totalQuizzes     = allQuizzes.size

            // Build study minutes per day from Firestore
            // We use todayStudyMinutes as a base — for full week we
            // store per-day breakdown in a subcollection studyLog
            val studyMinutesPerDay = fetchStudyMinutesPerDay(userId, weekStart)

            val totalStudyThisWeek = studyMinutesPerDay.sum()

            Result.success(
                WeeklyStats(
                    studyMinutesPerDay         = studyMinutesPerDay,
                    totalStudyMinutesThisWeek  = totalStudyThisWeek,
                    tasksCompletedThisWeek     = tasksThisWeek,
                    quizzesTakenThisWeek       = quizzesThisWeek,
                    averageQuizScore           = avgScore,
                    totalQuizXP                = totalQuizXP,
                    totalQuizzesTaken          = totalQuizzes
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Fetch per-day study minutes from studyLog ──────────────────────
    private suspend fun fetchStudyMinutesPerDay(
        userId: String,
        weekStart: Long
    ): List<Int> {
        return try {
            val minutesPerDay = MutableList(7) { 0 }
            val snapshot = userDoc(userId)
                .collection("studyLog")
                .whereGreaterThanOrEqualTo("timestamp", weekStart)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val timestamp = doc.getLong("timestamp") ?: return@forEach
                val minutes   = doc.getLong("minutes")?.toInt() ?: return@forEach
                val calendar  = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                // Convert Sunday=1..Saturday=7 to Mon=0..Sun=6
                val index = (dayOfWeek + 5) % 7
                minutesPerDay[index] += minutes
            }
            minutesPerDay
        } catch (e: Exception) {
            List(7) { 0 }
        }
    }

    // ── Log study session (called after each Pomodoro) ─────────────────
    suspend fun logStudySession(userId: String, minutes: Int): Result<Unit> {
        return try {
            val data = hashMapOf(
                "timestamp" to System.currentTimeMillis(),
                "minutes"   to minutes
            )
            userDoc(userId)
                .collection("studyLog")
                .add(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
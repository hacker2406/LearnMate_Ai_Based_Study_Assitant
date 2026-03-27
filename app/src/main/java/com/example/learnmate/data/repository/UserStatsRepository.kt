package com.example.learnmate.data.repository

import com.example.learnmate.data.model.UserStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserStatsRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun userDoc(userId: String) =
        firestore.collection("users").document(userId)

    // ── Real-time stats listener ───────────────────────────────────────
    fun getUserStats(userId: String): Flow<UserStats> = callbackFlow {
        val listener = userDoc(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val stats = snapshot?.toObject(UserStats::class.java)
                    ?: UserStats(userId = userId)
                trySend(stats)
            }
        awaitClose { listener.remove() }
    }

    // ── Initialize stats for new user ──────────────────────────────────
    suspend fun initializeStats(userId: String): Result<Unit> {
        return try {
            val stats = UserStats(
                userId = userId,
                xp = 0,
                level = 1,
                levelTitle = "Beginner",
                streak = 0,
                lastStudyDate = 0L,
                totalStudyMinutes = 0,
                todayStudyMinutes = 0,
                lastStudyDateString = ""
            )
            userDoc(userId).set(stats, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Award XP ───────────────────────────────────────────────────────
    suspend fun awardXP(userId: String, xpToAdd: Int): Result<Unit> {
        return try {
            val snapshot = userDoc(userId).get().await()
            val currentXP = snapshot.getLong("xp")?.toInt() ?: 0
            val newXP = currentXP + xpToAdd
            val newLevel = calculateLevel(newXP)
            val newTitle = getLevelTitle(newLevel)

            userDoc(userId).update(
                mapOf(
                    "xp" to newXP,
                    "level" to newLevel,
                    "levelTitle" to newTitle
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update streak on app open / study session ──────────────────────
    suspend fun updateStreak(userId: String): Result<Unit> {
        return try {
            val snapshot = userDoc(userId).get().await()
            val lastDateStr = snapshot.getString("lastStudyDateString") ?: ""
            val todayStr = getTodayString()

            val currentStreak = snapshot.getLong("streak")?.toInt() ?: 0
            val yesterdayStr = getYesterdayString()

            val newStreak = when (lastDateStr) {
                todayStr     -> currentStreak          // already updated today
                yesterdayStr -> currentStreak + 1      // consecutive day
                ""           -> 1                      // first time
                else         -> 1                      // streak broken
            }

            userDoc(userId).update(
                mapOf(
                    "streak" to newStreak,
                    "lastStudyDate" to System.currentTimeMillis(),
                    "lastStudyDateString" to todayStr
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Add study minutes ──────────────────────────────────────────────
    suspend fun addStudyMinutes(userId: String, minutes: Int): Result<Unit> {
        return try {
            val snapshot       = userDoc(userId).get().await()
            val total          = snapshot.getLong("totalStudyMinutes")?.toInt() ?: 0
            val todayTotal     = snapshot.getLong("todayStudyMinutes")?.toInt() ?: 0
            val lastDateStr    = snapshot.getString("lastStudyDateString") ?: ""
            val todayStr       = getTodayString()

            val newTodayMinutes = if (lastDateStr == todayStr) todayTotal + minutes
            else minutes

            userDoc(userId).update(
                mapOf(
                    "totalStudyMinutes" to total + minutes,
                    "todayStudyMinutes" to newTodayMinutes
                )
            ).await()

            // ── Also log to studyLog for weekly chart ──────────────────
            userDoc(userId).collection("studyLog").add(
                hashMapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "minutes"   to minutes
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Helper: Level calculation ──────────────────────────────────────
    fun calculateLevel(xp: Int): Int = (xp / 200) + 1

    fun getLevelTitle(level: Int): String = when {
        level >= 10 -> "Master"
        level >= 8  -> "Expert"
        level >= 5  -> "Scholar"
        level >= 3  -> "Learner"
        else        -> "Beginner"
    }

    fun xpForNextLevel(currentXP: Int): Int {
        val currentLevel = calculateLevel(currentXP)
        return currentLevel * 200
    }

    // ── Helper: Date strings ───────────────────────────────────────────
    private fun getTodayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun getYesterdayString(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}
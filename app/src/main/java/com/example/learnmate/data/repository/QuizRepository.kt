package com.example.learnmate.data.repository

import com.example.learnmate.data.model.QuizHistory
import com.example.learnmate.data.remote.QuizQuestion
import com.example.learnmate.data.remote.RetrofitClient
import com.example.learnmate.data.remote.QuizRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class QuizRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val api = RetrofitClient.apiService

    // ── Firestore reference ────────────────────────────────────────────
    private fun quizHistoryCollection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("quizHistory")

    // ── Generate quiz from topic via AI ───────────────────────────────
    suspend fun generateQuizFromTopic(
        topic: String,
        numQuestions: Int
    ): Result<List<QuizQuestion>> {
        return try {
            val response = api.generateQuiz(QuizRequest(topic, numQuestions))
            Result.success(response.questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Generate quiz from note content via AI ────────────────────────
    suspend fun generateQuizFromNote(
        noteContent: String,
        numQuestions: Int
    ): Result<List<QuizQuestion>> {
        return try {
            val topic = "the following notes content:\n$noteContent"
            val response = api.generateQuiz(QuizRequest(topic, numQuestions))
            Result.success(response.questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Save quiz result to Firestore ─────────────────────────────────
    suspend fun saveQuizResult(history: QuizHistory): Result<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val toSave = history.copy(id = id)
            quizHistoryCollection(history.userId)
                .document(id)
                .set(toSave)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Get quiz history real-time ────────────────────────────────────
    fun getQuizHistory(userId: String): Flow<List<QuizHistory>> = callbackFlow {
        val listener = quizHistoryCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val history = snapshot?.documents?.mapNotNull {
                    it.toObject(QuizHistory::class.java)
                } ?: emptyList()
                trySend(history)
            }
        awaitClose { listener.remove() }
    }
}
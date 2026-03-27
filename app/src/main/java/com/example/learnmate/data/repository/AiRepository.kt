package com.example.learnmate.data.repository

import com.example.learnmate.data.remote.ChatRequest
import com.example.learnmate.data.remote.QuizRequest
import com.example.learnmate.data.remote.RetrofitClient
import com.example.learnmate.data.remote.StudyPlanRequest
import com.example.learnmate.data.remote.SummarizeRequest
import com.example.learnmate.data.remote.QuizQuestion

class AiRepository {

    private val api = RetrofitClient.apiService

    // ── Chat ───────────────────────────────────────────────────────────
    suspend fun chat(
        message: String,
        history: List<Map<String, String>> = emptyList()
    ): Result<String> {
        return try {
            val response = api.chat(ChatRequest(message, history))
            Result.success(response.reply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Summarize ──────────────────────────────────────────────────────
    suspend fun summarize(text: String): Result<String> {
        return try {
            val response = api.summarize(SummarizeRequest(text))
            Result.success(response.summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Quiz ───────────────────────────────────────────────────────────
    suspend fun generateQuiz(
        topic: String,
        numQuestions: Int = 5
    ): Result<List<QuizQuestion>> {
        return try {
            val response = api.generateQuiz(QuizRequest(topic, numQuestions))
            Result.success(response.questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Study Plan ─────────────────────────────────────────────────────
    suspend fun generateStudyPlan(
        subject: String,
        duration: String,
        goal: String
    ): Result<String> {
        return try {
            val response = api.generateStudyPlan(
                StudyPlanRequest(subject, duration, goal)
            )
            Result.success(response.plan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
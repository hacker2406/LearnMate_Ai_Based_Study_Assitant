package com.example.learnmate.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

// ── Request bodies ─────────────────────────────────────────────────────
data class ChatRequest(
    val message: String,
    val history: List<Map<String, String>> = emptyList()
)

data class SummarizeRequest(val text: String)

data class QuizRequest(
    val topic: String,
    val numQuestions: Int = 5
)

data class StudyPlanRequest(
    val subject: String,
    val duration: String = "1 week",
    val goal: String = "understand the basics"
)

// ── Response bodies ────────────────────────────────────────────────────
data class ChatResponse(val reply: String)
data class SummarizeResponse(val summary: String)
data class QuizResponse(val questions: List<QuizQuestion>)
data class StudyPlanResponse(val plan: String)

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = ""
)

// ── Retrofit interface ─────────────────────────────────────────────────
interface ApiService {

    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @POST("summarize")
    suspend fun summarize(@Body request: SummarizeRequest): SummarizeResponse

    @POST("quiz")
    suspend fun generateQuiz(@Body request: QuizRequest): QuizResponse

    @POST("studyplan")
    suspend fun generateStudyPlan(@Body request: StudyPlanRequest): StudyPlanResponse
}
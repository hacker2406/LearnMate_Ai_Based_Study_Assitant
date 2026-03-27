package com.example.learnmate.ui.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnmate.data.model.ChatMessage
import com.example.learnmate.data.model.MessageType
import com.example.learnmate.data.remote.QuizQuestion
import com.example.learnmate.data.repository.AiRepository
import kotlinx.coroutines.launch
import java.util.UUID

class AiViewModel : ViewModel() {

    private val repository = AiRepository()

    // ── Chat messages list ─────────────────────────────────────────────
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    // ── Loading state ──────────────────────────────────────────────────
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ── Current mode ──────────────────────────────────────────────────
    private val _currentMode = MutableLiveData(AiMode.CHAT)
    val currentMode: LiveData<AiMode> = _currentMode

    // ── Quiz state ─────────────────────────────────────────────────────
    private val _quizQuestions = MutableLiveData<List<QuizQuestion>>()
    val quizQuestions: LiveData<List<QuizQuestion>> = _quizQuestions

    // ── Error ──────────────────────────────────────────────────────────
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ── Conversation history for context ──────────────────────────────
    private val conversationHistory = mutableListOf<Map<String, String>>()

    // ── Set mode ───────────────────────────────────────────────────────
    fun setMode(mode: AiMode) {
        _currentMode.value = mode
        // Add a system message showing mode change
        val modeMessage = when (mode) {
            AiMode.CHAT       -> "💬 Chat mode — ask me anything!"
            AiMode.SUMMARIZE  -> "📝 Summarize mode — paste text to summarize"
            AiMode.QUIZ       -> "🧠 Quiz mode — enter a topic to generate a quiz"
            AiMode.STUDY_PLAN -> "📚 Study Plan mode — tell me what you want to study"
        }
        addBotMessage(modeMessage, MessageType.CHAT)
    }

    // ── Send chat message ──────────────────────────────────────────────
    fun sendMessage(userInput: String) {
        if (userInput.isBlank()) return

        addUserMessage(userInput)
        _isLoading.value = true

        viewModelScope.launch {
            when (_currentMode.value) {
                AiMode.CHAT       -> handleChat(userInput)
                AiMode.SUMMARIZE  -> handleSummarize(userInput)
                AiMode.QUIZ       -> handleQuiz(userInput)
                AiMode.STUDY_PLAN -> handleStudyPlan(userInput)
                else              -> handleChat(userInput)
            }
            _isLoading.value = false
        }
    }

    // ── Chat handler ───────────────────────────────────────────────────
    private suspend fun handleChat(message: String) {
        val result = repository.chat(message, conversationHistory)
        result.onSuccess { reply ->
            // Update history for context
            conversationHistory.add(mapOf("role" to "user", "parts" to message))
            conversationHistory.add(mapOf("role" to "model", "parts" to reply))
            // Keep history manageable (last 10 exchanges)
            if (conversationHistory.size > 20) {
                conversationHistory.removeAt(0)
                conversationHistory.removeAt(0)
            }
            addBotMessage(reply, MessageType.CHAT)
        }.onFailure {
            addBotMessage(
                "Sorry, I couldn't connect. Please check your internet and try again.",
                MessageType.CHAT
            )
        }
    }

    // ── Summarize handler ──────────────────────────────────────────────
    private suspend fun handleSummarize(text: String) {
        val result = repository.summarize(text)
        result.onSuccess { summary ->
            addBotMessage(summary, MessageType.SUMMARY)
        }.onFailure {
            addBotMessage("Sorry, I couldn't summarize that. Please try again.", MessageType.CHAT)
        }
    }

    // ── Quiz handler ───────────────────────────────────────────────────
    private suspend fun handleQuiz(topic: String) {
        val result = repository.generateQuiz(topic)
        result.onSuccess { questions ->
            _quizQuestions.value = questions
            addBotMessage(
                "🧠 Quiz on \"$topic\" is ready! I generated ${questions.size} questions.",
                MessageType.QUIZ
            )
        }.onFailure {
            addBotMessage("Sorry, I couldn't generate a quiz. Please try again.", MessageType.CHAT)
        }
    }

    // ── Study Plan handler ─────────────────────────────────────────────
    private suspend fun handleStudyPlan(input: String) {
        val result = repository.generateStudyPlan(
            subject  = input,
            duration = "1 week",
            goal     = "understand and master the topic"
        )
        result.onSuccess { plan ->
            addBotMessage(plan, MessageType.STUDY_PLAN)
        }.onFailure {
            addBotMessage(
                "Sorry, I couldn't generate a study plan. Please try again.",
                MessageType.CHAT
            )
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────
    private fun addUserMessage(text: String) {
        val current = _messages.value.orEmpty().toMutableList()
        current.add(
            ChatMessage(
                id        = UUID.randomUUID().toString(),
                text      = text,
                isUser    = true,
                type      = MessageType.CHAT
            )
        )
        _messages.value = current
    }

    private fun addBotMessage(text: String, type: MessageType) {
        val current = _messages.value.orEmpty().toMutableList()
        current.add(
            ChatMessage(
                id     = UUID.randomUUID().toString(),
                text   = text,
                isUser = false,
                type   = type
            )
        )
        _messages.value = current
    }

    fun clearChat() {
        _messages.value = emptyList()
        conversationHistory.clear()
    }
}

enum class AiMode {
    CHAT, SUMMARIZE, QUIZ, STUDY_PLAN
}
package com.example.learnmate.data.model

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.CHAT
)

enum class MessageType {
    CHAT, SUMMARY, QUIZ, STUDY_PLAN
}
package com.example.learnmate.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnmate.data.model.Note
import com.example.learnmate.data.model.QuizHistory
import com.example.learnmate.data.remote.QuizQuestion
import com.example.learnmate.data.repository.NoteRepository
import com.example.learnmate.data.repository.QuizRepository
import com.example.learnmate.data.repository.UserStatsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {

    private val quizRepo  = QuizRepository()
    private val noteRepo  = NoteRepository()
    private val statsRepo = UserStatsRepository()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ── Quiz setup state ───────────────────────────────────────────────
    private val _quizState = MutableLiveData<QuizState>(QuizState.Idle)
    val quizState: LiveData<QuizState> = _quizState

    // ── Questions list ─────────────────────────────────────────────────
    private val _questions = MutableLiveData<List<QuizQuestion>>()
    val questions: LiveData<List<QuizQuestion>> = _questions

    // ── Current question index ─────────────────────────────────────────
    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    // ── Selected answer for current question ──────────────────────────
    private val _selectedAnswer = MutableLiveData<Int?>()
    val selectedAnswer: LiveData<Int?> = _selectedAnswer

    // ── Score tracking ─────────────────────────────────────────────────
    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    // ── Notes list for picker ──────────────────────────────────────────
    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    // ── Current quiz topic ─────────────────────────────────────────────
    var currentTopic = ""

    // ── User answers tracking ──────────────────────────────────────────
    val userAnswers = mutableListOf<Int>()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            val notesList = noteRepo.getNotes(userId).first()
            _notes.value = notesList
        }
    }

    // ── Generate quiz from topic ───────────────────────────────────────
    fun generateFromTopic(topic: String, numQuestions: Int) {
        currentTopic = topic
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            val result = quizRepo.generateQuizFromTopic(topic, numQuestions)
            handleGenerateResult(result)
        }
    }

    // ── Generate quiz from note ────────────────────────────────────────
    fun generateFromNote(note: Note, numQuestions: Int) {
        currentTopic = note.title.ifEmpty { "My Note" }
        val content = "${note.title}\n${note.content}"
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            val result = quizRepo.generateQuizFromNote(content, numQuestions)
            handleGenerateResult(result)
        }
    }

    private fun handleGenerateResult(result: Result<List<QuizQuestion>>) {
        result.onSuccess { questions ->
            if (questions.isEmpty()) {
                _quizState.value = QuizState.Error("No questions generated. Try again.")
                return
            }
            _questions.value = questions
            _currentIndex.value = 0
            _score.value = 0
            userAnswers.clear()
            _quizState.value = QuizState.Playing
        }.onFailure {
            _quizState.value = QuizState.Error(
                it.message ?: "Failed to generate quiz. Check connection."
            )
        }
    }

    // ── Submit answer ──────────────────────────────────────────────────
    fun submitAnswer(selectedIndex: Int) {
        _selectedAnswer.value = selectedIndex
        val question = _questions.value?.get(_currentIndex.value ?: 0) ?: return
        userAnswers.add(selectedIndex)
        if (selectedIndex == question.correctIndex) {
            _score.value = (_score.value ?: 0) + 1
        }
    }

    // ── Next question ──────────────────────────────────────────────────
    fun nextQuestion() {
        val current = _currentIndex.value ?: 0
        val total   = _questions.value?.size ?: 0
        _selectedAnswer.value = null

        if (current + 1 >= total) {
            // Quiz finished
            saveResult()
            _quizState.value = QuizState.Finished
        } else {
            _currentIndex.value = current + 1
        }
    }

    // ── Save result to Firestore + award XP ───────────────────────────
    private fun saveResult() {
        viewModelScope.launch {
            val score      = _score.value ?: 0
            val total      = _questions.value?.size ?: 0
            val xpEarned   = score * 10  // 10 XP per correct answer

            quizRepo.saveQuizResult(
                QuizHistory(
                    userId         = userId,
                    topic          = currentTopic,
                    score          = score,
                    totalQuestions = total,
                    xpEarned       = xpEarned,
                    timestamp      = System.currentTimeMillis()
                )
            )
            statsRepo.awardXP(userId, xpEarned)
        }
    }

    // ── Reset quiz ─────────────────────────────────────────────────────
    fun resetQuiz() {
        _quizState.value  = QuizState.Idle
        _currentIndex.value = 0
        _score.value      = 0
        _selectedAnswer.value = null
        userAnswers.clear()
    }
}

// ── State ──────────────────────────────────────────────────────────────
sealed class QuizState {
    object Idle     : QuizState()
    object Loading  : QuizState()
    object Playing  : QuizState()
    object Finished : QuizState()
    data class Error(val message: String) : QuizState()
}
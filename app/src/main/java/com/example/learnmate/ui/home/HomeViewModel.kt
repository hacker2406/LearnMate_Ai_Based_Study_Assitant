package com.example.learnmate.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.learnmate.data.model.UserStats
import com.example.learnmate.data.repository.NoteRepository
import com.example.learnmate.data.repository.TaskRepository
import com.example.learnmate.data.repository.UserStatsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val statsRepo = UserStatsRepository()
    private val noteRepo = NoteRepository()
    private val taskRepo = TaskRepository()

    // ── Combined home data ─────────────────────────────────────────────
    val homeData: LiveData<HomeData> = combine(
        statsRepo.getUserStats(userId),
        noteRepo.getNotes(userId),
        taskRepo.getTasks(userId)
    ) { stats, notes, tasks ->
        val completedToday = tasks.count { task ->
            task.isCompleted &&
                    isToday(task.timestamp)
        }
        val pendingTasks = tasks.count { !it.isCompleted }

        HomeData(
            stats = stats,
            notesCount = notes.size,
            pendingTasksCount = pendingTasks,
            completedTodayCount = completedToday,
            totalTasksCount = tasks.size
        )
    }.asLiveData()

    // ── Focus timer state ──────────────────────────────────────────────
    private val _timerState = MutableLiveData<TimerState>(TimerState.Idle)
    val timerState: LiveData<TimerState> = _timerState

    private var timerJob: kotlinx.coroutines.Job? = null
    private var remainingSeconds = 25 * 60  // 25 min pomodoro

    fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            _timerState.value = TimerState.Running(remainingSeconds)
            while (remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                remainingSeconds--
                _timerState.value = TimerState.Running(remainingSeconds)
            }
            // Session complete — award XP + study minutes
            onPomodoroComplete()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.Paused(remainingSeconds)
    }

    fun resetTimer() {
        timerJob?.cancel()
        remainingSeconds = 25 * 60
        _timerState.value = TimerState.Idle
    }

    private fun onPomodoroComplete() {
        viewModelScope.launch {
            statsRepo.awardXP(userId, 30)
            statsRepo.addStudyMinutes(userId, 25)
            statsRepo.updateStreak(userId)
            remainingSeconds = 25 * 60
            _timerState.value = TimerState.Completed
        }
    }

    // ── Update streak on home open ─────────────────────────────────────
    fun updateStreakOnOpen() {
        viewModelScope.launch {
            statsRepo.updateStreak(userId)
        }
    }

    // ── Helper ─────────────────────────────────────────────────────────
    private fun isToday(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % 86400000)
        return timestamp >= startOfDay
    }
}

// ── Data class for combined home screen data ───────────────────────────
data class HomeData(
    val stats: UserStats = UserStats(),
    val notesCount: Int = 0,
    val pendingTasksCount: Int = 0,
    val completedTodayCount: Int = 0,
    val totalTasksCount: Int = 0
)

// ── Timer states ───────────────────────────────────────────────────────
sealed class TimerState {
    object Idle : TimerState()
    object Completed : TimerState()
    data class Running(val remainingSeconds: Int) : TimerState()
    data class Paused(val remainingSeconds: Int) : TimerState()
}
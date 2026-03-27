package com.example.learnmate.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnmate.data.model.UserStats
import com.example.learnmate.data.model.WeeklyStats
import com.example.learnmate.data.repository.ProgressRepository
import com.example.learnmate.data.repository.UserStatsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProgressViewModel : ViewModel() {

    private val userId       = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val progressRepo = ProgressRepository()
    private val statsRepo    = UserStatsRepository()

    // ── Weekly stats ───────────────────────────────────────────────────
    private val _weeklyStats = MutableLiveData<WeeklyStats>()
    val weeklyStats: LiveData<WeeklyStats> = _weeklyStats

    // ── User stats ─────────────────────────────────────────────────────
    private val _userStats = MutableLiveData<UserStats>()
    val userStats: LiveData<UserStats> = _userStats

    // ── Loading state ──────────────────────────────────────────────────
    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadAllStats()
    }

    fun loadAllStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load user stats (XP, level, streak)
                val stats = statsRepo.getUserStats(userId).first()
                _userStats.value = stats

                // Load weekly stats
                val weekly = progressRepo.getWeeklyStats(userId)
                weekly.onSuccess { _weeklyStats.value = it }

            } catch (e: Exception) {
                // Use defaults on error
                _weeklyStats.value = WeeklyStats()
                _userStats.value   = UserStats()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() = loadAllStats()
}
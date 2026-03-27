package com.example.learnmate.ui.profile

import androidx.lifecycle.LiveData
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

class ProfileViewModel : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val auth   = FirebaseAuth.getInstance()

    private val statsRepo = UserStatsRepository()
    private val noteRepo  = NoteRepository()
    private val taskRepo  = TaskRepository()

    // ── Combined profile data ──────────────────────────────────────────
    val profileData: LiveData<ProfileData> = combine(
        statsRepo.getUserStats(userId),
        noteRepo.getNotes(userId),
        taskRepo.getTasks(userId)
    ) { stats, notes, tasks ->
        ProfileData(
            stats          = stats,
            totalNotes     = notes.size,
            totalTasksDone = tasks.count { it.isCompleted },
            totalTasks     = tasks.size
        )
    }.asLiveData()

    // ── Logout ─────────────────────────────────────────────────────────
    fun logout() {
        auth.signOut()
    }
}

data class ProfileData(
    val stats: UserStats          = UserStats(),
    val totalNotes: Int           = 0,
    val totalTasksDone: Int       = 0,
    val totalTasks: Int           = 0
)
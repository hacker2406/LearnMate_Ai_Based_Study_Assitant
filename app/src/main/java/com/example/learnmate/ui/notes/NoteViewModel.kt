package com.example.learnmate.ui.notes


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.learnmate.data.model.Note
import com.example.learnmate.data.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class NoteViewModel : ViewModel() {

    private val repository = NoteRepository()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ── Real-time notes list from Firestore ────────────────────────────
    val notes: LiveData<List<Note>> =
        repository.getNotes(userId).asLiveData()

    // ── UI state ───────────────────────────────────────────────────────
    private val _noteState = MutableLiveData<NoteState>(NoteState.Idle)
    val noteState: LiveData<NoteState> = _noteState

    // ── Save note (create or update) ───────────────────────────────────
    fun saveNote(title: String, content: String, existingId: String? = null) {

        if (title.isBlank() && content.isBlank()) {
            _noteState.value = NoteState.Error("Note cannot be empty")
            return
        }

        viewModelScope.launch {
            _noteState.value = NoteState.Loading

            val note = Note(
                id = existingId ?: UUID.randomUUID().toString(),
                userId = userId,
                title = title.trim(),
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )

            val result = repository.saveNote(note)
            _noteState.value = if (result.isSuccess) {
                NoteState.Success("Note saved!")
            } else {
                NoteState.Error(result.exceptionOrNull()?.message ?: "Failed to save")
            }
        }
    }

    // ── Delete note ────────────────────────────────────────────────────
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _noteState.value = NoteState.Loading
            val result = repository.deleteNote(userId, note.id)
            _noteState.value = if (result.isSuccess) {
                NoteState.Success("Note deleted")
            } else {
                NoteState.Error(result.exceptionOrNull()?.message ?: "Failed to delete")
            }
        }
    }
}

// ── State sealed class ─────────────────────────────────────────────────
sealed class NoteState {
    object Idle : NoteState()
    object Loading : NoteState()
    data class Success(val message: String) : NoteState()
    data class Error(val message: String) : NoteState()
}
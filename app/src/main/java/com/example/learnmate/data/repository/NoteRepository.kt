package com.example.learnmate.data.repository


import com.example.learnmate.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NoteRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // ── Firestore collection reference ─────────────────────────────────
    private fun notesCollection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("notes")

    // ── Real-time listener using Flow ──────────────────────────────────
    fun getNotes(userId: String): Flow<List<Note>> = callbackFlow {
        val listener = notesCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents?.mapNotNull {
                    it.toObject(Note::class.java)
                } ?: emptyList()
                trySend(notes)
            }
        awaitClose { listener.remove() }
    }

    // ── Save (create or update) ────────────────────────────────────────
    suspend fun saveNote(note: Note): Result<Unit> {
        return try {
            val noteId = note.id.ifEmpty { UUID.randomUUID().toString() }
            val finalNote = note.copy(id = noteId)
            notesCollection(note.userId)
                .document(noteId)
                .set(finalNote)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────
    suspend fun deleteNote(userId: String, noteId: String): Result<Unit> {
        return try {
            notesCollection(userId)
                .document(noteId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
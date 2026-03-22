package com.example.learnmate.data.repository

import com.example.learnmate.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TaskRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // ── Firestore collection reference ─────────────────────────────────
    private fun tasksCollection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("tasks")

    // ── Real-time listener using Flow ──────────────────────────────────
    fun getTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val listener = tasksCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull {
                    it.toObject(Task::class.java)
                } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    // ── Save task ──────────────────────────────────────────────────────
    suspend fun saveTask(task: Task): Result<Unit> {
        return try {
            val taskId = task.id.ifEmpty { UUID.randomUUID().toString() }
            val finalTask = task.copy(id = taskId)
            tasksCollection(task.userId)
                .document(taskId)
                .set(finalTask)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Toggle complete ────────────────────────────────────────────────
    suspend fun toggleTaskComplete(
        userId: String,
        taskId: String,
        isCompleted: Boolean
    ): Result<Unit> {
        return try {
            tasksCollection(userId)
                .document(taskId)
                .update("isCompleted", isCompleted)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Delete task ────────────────────────────────────────────────────
    suspend fun deleteTask(userId: String, taskId: String): Result<Unit> {
        return try {
            tasksCollection(userId)
                .document(taskId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
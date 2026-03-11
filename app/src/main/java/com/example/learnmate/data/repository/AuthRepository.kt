package com.example.learnmate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.learnmate.ui.auth.AuthState

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun register(
        name: String,
        email: String,
        password: String,
        callback: (AuthState) -> Unit
    ) {
        callback(AuthState.Loading)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val firebaseUser = auth.currentUser

                    firebaseUser?.let { user ->

                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "name" to name,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                callback(AuthState.Success("Registration successful"))
                            }
                            .addOnFailureListener { e ->
                                callback(AuthState.Error(e.message ?: "Firestore error"))
                            }
                    }

                } else {
                    callback(AuthState.Error(task.exception?.message ?: "Registration failed"))
                }
            }
    }

    fun login(
        email: String,
        password: String,
        callback: (AuthState) -> Unit
    ) {
        callback(AuthState.Loading)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    callback(AuthState.Success("Login successful"))
                } else {
                    callback(AuthState.Error(task.exception?.message ?: "Login failed"))
                }
            }
    }
}
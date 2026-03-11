package com.example.learnmate.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnmate.data.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun register(name: String, email: String, password: String) {

        repository.register(name, email, password) { state ->
            _authState.postValue(state)
        }
    }

    fun login(email: String, password: String) {

        repository.login(email, password) { state ->
            _authState.postValue(state)
        }
    }
}
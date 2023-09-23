package com.example.movilesapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    fun signInWithEmailAndPassword(email: String, password: String, onHomeSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                setLoading(true)
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Login", "Login Successful")
                            onHomeSuccess()
                        } else {
                            Log.d("Login", "Login Fail: ${task.exception?.message}")
                            _errorMessageLiveData.value = "Invalid Credentials"
                        }
                    }
            } catch (ex: Exception) {
                Log.d("Login", "Error Login: ${ex.message}")
                val errorMessage = when {
                    ex.message?.contains("Given String is empty or null") == true -> {
                        "Email or Password are Empty"
                    }
                    else -> {
                        "An error occurred while logging in"
                    }
                }
                _errorMessageLiveData.value = errorMessage
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}
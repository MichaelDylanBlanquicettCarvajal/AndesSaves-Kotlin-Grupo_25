package com.example.movilesapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.repositories.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    fun signInWithEmailAndPassword(email: String, password: String, onHomeSuccess: () -> Unit) {
        _errorMessageLiveData.value = ""
        viewModelScope.launch {
            try {
                setLoading(true)
                val (success, errorMessage) = authRepository.signInWithEmailAndPassword(
                    email,
                    password
                )
                if (success) {
                    Log.d("Login", "Login Successful")
                    onHomeSuccess()
                } else {
                    Log.d("Login", "Login Failed")
                    val adjustedErrorMessage = when {
                        errorMessage?.contains("Given String is empty or null") == true -> {
                            "The email or password are empty"
                        }
                        errorMessage?.contains("INVALID_LOGIN_CREDENTIALS") == true -> {
                            "Invalid Credentials"
                        }
                        else -> {
                            errorMessage
                                ?: "Login Failed"
                        }
                    }
                    _errorMessageLiveData.value = adjustedErrorMessage
                }
            } catch (ex: Exception) {
                Log.d("Login", "Error Login: ${ex.message}")
                _errorMessageLiveData.value = "An error occurred while logging in"
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}
package com.example.movilesapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.repositories.AuthRepository
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.AuthRepositoryImpl
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(context: Context) : ViewModel() {

    private val userRepository: UserRepository = UserRepositoryImpl()
    private val authRepository: AuthRepository = AuthRepositoryImpl(context)

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    fun signInWithEmailAndPassword(email: String, password: String, onHomeSuccess: () -> Unit) {
        _errorMessageLiveData.value = ""
        viewModelScope.launch(Dispatchers.Main) {
            try {
                setLoading(true)
                val (success, message) = authRepository.signInWithEmailAndPassword(email, password)

                if (success) {
                    Log.d("Login", "Login Successful")
                    message?.let { userRepository.getUserInformation(it) }
                    onHomeSuccess()
                } else {
                    Log.d("Login", "Login Failed")
                    handleLoginFailure(message)
                }
            } catch (ex: Exception) {
                Log.d("Login", "Error Login: ${ex.message}")
                _errorMessageLiveData.value = "An error occurred while logging in"
            } finally {
                setLoading(false)
            }
        }
    }

    private fun handleLoginFailure(message: String?) {
        val adjustedErrorMessage = when {
            message?.contains("Given String is empty or null") == true -> {
                "The email or password are empty"
            }
            message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> {
                "Invalid Credentials"
            }
            else -> {
                message ?: "Login Failed"
            }
        }
        _errorMessageLiveData.value = adjustedErrorMessage
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }
}

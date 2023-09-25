package com.example.movilesapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.User
import com.example.movilesapp.model.repositories.AuthRepository
import com.example.movilesapp.model.repositories.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    val errorMessageLiveData = MutableLiveData<String>()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    fun registerWithEmailAndPassword(
        email: String,
        name: String,
        phone: String,
        password: String,
        confirmPassword: String,
        onHomeSuccess: () -> Unit
    ) {
        val intPhone = phone.toIntOrNull()

        if (name.isEmpty() || name.isBlank()) {
            errorMessageLiveData.value = "Name cannot be empty"
            return
        }

        if (intPhone == null) {
            errorMessageLiveData.value = "Phone is not a valid number"
            return
        }

        if (password != confirmPassword) {
            errorMessageLiveData.value = "Passwords do not match."
            return
        }

        viewModelScope.launch {
            try {
                setLoading(true)
                val user = authRepository.registerUser(email, password)
                if (user != null) {
                    val userCreated = userRepository.createUser(
                        User(userId = user.uid, name = name, phone = intPhone, email = email, balance = 0)
                    )
                    if (userCreated) {
                        onHomeSuccess()
                    } else {
                        errorMessageLiveData.value = "Failed to create user data"
                    }
                } else {
                    errorMessageLiveData.value = "Registration failed"
                }
            } catch (e: Exception) {
                errorMessageLiveData.value = e.message ?: "An error occurred during registration"
            }
            finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}

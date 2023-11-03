package com.example.movilesapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.User
import com.example.movilesapp.model.repositories.AuthRepository
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.AuthRepositoryImpl
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import kotlinx.coroutines.launch

class RegisterViewModel(context: Context): ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl(context)
    private val authRepository: AuthRepository = AuthRepositoryImpl()
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
        val numberPhone = phone.toLongOrNull()

        if (name.isEmpty() || name.isBlank()) {
            errorMessageLiveData.value = "Name cannot be empty"
            return
        }

        if (numberPhone == null) {
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
                        User(
                            userId = user.uid,
                            name = name,
                            phone = numberPhone,
                            email = email,
                            balance = 0.0
                        )
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
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}

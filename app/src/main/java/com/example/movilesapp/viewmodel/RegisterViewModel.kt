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
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel(context: Context) : ViewModel() {

    private val userRepository: UserRepository = UserRepositoryImpl()
    private val authRepository: AuthRepository = AuthRepositoryImpl(context)

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

        if (name.isEmpty()) {
            handleValidationError("Name cannot be empty")
            return
        }

        if (numberPhone == null) {
            handleValidationError("Phone is not a valid number")
            return
        }

        if (password != confirmPassword) {
            handleValidationError("Passwords do not match.")
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            try {
                setLoading(true)
                val user = authRepository.registerUser(email, password)
                if (user != null) {
                    createUserAndNavigateToHome(user, name, numberPhone, email, onHomeSuccess)
                } else {
                    handleRegistrationFailure("Registration failed")
                }
            } catch (e: Exception) {
                handleRegistrationFailure(e.message ?: "An error occurred during registration")
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun createUserAndNavigateToHome(
        user: FirebaseUser,
        name: String,
        numberPhone: Long,
        email: String,
        onHomeSuccess: () -> Unit
    ) {
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
            handleRegistrationFailure("Failed to create user data")
        }
    }

    private fun handleValidationError(message: String) {
        errorMessageLiveData.value = message
    }

    private fun handleRegistrationFailure(message: String) {
        errorMessageLiveData.value = message
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}

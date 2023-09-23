package com.example.movilesapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

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
            _errorMessageLiveData.value = "Name cannot be empty"
            return
        }

        if (intPhone == null) {
            _errorMessageLiveData.value = "Phone is not a valid number"
            return
        }

        if (password != confirmPassword) {
            _errorMessageLiveData.value = "Passwords do not match."
            return
        }
        viewModelScope.launch {
            try {
                setLoading(true)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            createUser(name, intPhone, email)
                            Log.d("Register", "Register Successful")
                            onHomeSuccess()
                        } else {
                            Log.d("Register", "Register Fail: ${task.exception?.message}")
                            _errorMessageLiveData.value = "${task.exception?.message}"
                        }
                    }
            } catch (ex: Exception) {
                Log.d("Register", "Error Register: ${ex.message}")
                val errorMessage = when {
                    ex.message?.contains("Given String is empty or null") == true -> {
                        "Email or Password are Empty"
                    }
                    else -> {
                        "An error occurred while registering in"
                    }
                }
                _errorMessageLiveData.value = errorMessage
            } finally {
                setLoading(false)
            }
        }
    }

    private fun createUser(name: String, phone: Int, email: String) {
        val userId = auth.currentUser?.uid
        val user = mutableMapOf<String, Any>()

        user["userId"] = userId.toString()
        user["name"] = name
        user["phone"] = phone
        user["email"] = email
        user["balance"] = 0

        FirebaseFirestore.getInstance().collection("users")
            .add(user)
            .addOnSuccessListener { Log.d("Register", "User created success ${it.id}") }
            .addOnFailureListener { Log.d("Register", "User created failed ${it}") }
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}
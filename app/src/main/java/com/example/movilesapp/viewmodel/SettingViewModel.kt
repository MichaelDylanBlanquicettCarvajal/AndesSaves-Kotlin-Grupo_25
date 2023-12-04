package com.example.movilesapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.repositories.AuthRepository
import com.example.movilesapp.model.repositories.implementations.AuthRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(context: Context) : ViewModel() {

    private val authRepository: AuthRepository = AuthRepositoryImpl(context)
    val navigateToLoginActivity = MutableLiveData<Boolean>()

    fun signOut() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                authRepository.signOut()
                navigateToLoginActivity.value = true
            } catch (e: Exception) {
                handleSignOutException(e)
            }
        }
    }

    private fun handleSignOutException(exception: Exception) {
        Log.d("SignOut", "Exception in SignOut: ${exception.message}")
        // Handle the exception as needed
    }
}

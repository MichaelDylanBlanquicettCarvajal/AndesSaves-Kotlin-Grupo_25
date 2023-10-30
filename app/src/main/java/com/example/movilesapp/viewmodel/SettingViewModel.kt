package com.example.movilesapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.repositories.AuthRepository
import com.example.movilesapp.model.repositories.implementations.AuthRepositoryImpl
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {

    private val authRepository: AuthRepository = AuthRepositoryImpl()
    val navigateToLoginActivity = MutableLiveData<Boolean>()

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                navigateToLoginActivity.value = true
            } catch (e: Exception) {
                Log.d("SignOut", "Exception in SignOut" + e.message.toString())
            }
        }
    }

}
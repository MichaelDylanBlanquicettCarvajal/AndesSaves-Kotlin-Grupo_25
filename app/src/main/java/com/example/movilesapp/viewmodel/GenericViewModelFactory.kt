package com.example.movilesapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenericViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            val constructor = modelClass.getConstructor(Context::class.java)
            return constructor.newInstance(context)
        } catch (e: Exception) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

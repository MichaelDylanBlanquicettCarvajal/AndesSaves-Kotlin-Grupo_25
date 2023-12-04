package com.example.movilesapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenericViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
            val constructor = modelClass.getConstructor(Context::class.java)
            constructor.newInstance(context)
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("ViewModel class must have a constructor with a single Context parameter.", e)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error creating ViewModel", e)
        } as T
    }
}

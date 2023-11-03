package com.example.movilesapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(context: Context) : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl(context)

    private val _transactionsLiveData = MutableLiveData<List<Transaction>>()
    val transactionsLiveData: LiveData<List<Transaction>> get() = _transactionsLiveData

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

    fun getTransactionsOfUser() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val transactions = userRepository.getTransactionsOfUser()
                _transactionsLiveData.value = transactions
            } catch (e: Exception) {
                _errorMessageLiveData.value = "Error getting user transactions: ${e.message.toString()}"
            }
        }
    }
}

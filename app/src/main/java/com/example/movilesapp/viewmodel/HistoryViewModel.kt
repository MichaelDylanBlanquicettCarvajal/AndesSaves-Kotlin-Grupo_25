package com.example.movilesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _transactionsLiveData = MutableLiveData<List<Transaction>>()
    val transactionsLiveData: LiveData<List<Transaction>> get() = _transactionsLiveData

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

    fun getTransactionsOfUser() {
        viewModelScope.launch {
            try {
                val transactions = userRepository.getTransactionsOfUser()
                _transactionsLiveData.value = transactions
            } catch (e: Exception) {
                _errorMessageLiveData.value = "Error getting user transactions: ${e.message.toString()}"
            }
        }
    }
}

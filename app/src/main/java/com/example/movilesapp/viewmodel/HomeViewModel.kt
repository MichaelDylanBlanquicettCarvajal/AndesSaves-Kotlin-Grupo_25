package com.example.movilesapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl()

    private val _balanceLiveData = MutableLiveData<String>()
    val balanceLiveData: LiveData<String> get() = _balanceLiveData

    fun getTransactionsOfUser() {
        viewModelScope.launch {
            try {
                val transactions = userRepository.getTransactionsOfUser()
                calculateBalance(transactions)
            } catch (e: Exception) {
                Log.d("Transactions", "Error getting user transactions: ${e.message.toString()}")
            }
        }
    }

    private fun calculateBalance(transactions: List<Transaction>) {
        var balance = 0.0
        for (transaction in transactions) {
            if (transaction.type == "Income") {
                balance += transaction.amount
            } else if (transaction.type == "Expense") {
                balance -= transaction.amount
            }
        }
        _balanceLiveData.postValue(balance.toString())
    }
}

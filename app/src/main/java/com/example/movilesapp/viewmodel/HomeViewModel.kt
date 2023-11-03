package com.example.movilesapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import kotlinx.coroutines.launch

class HomeViewModel(context: Context) : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl(context)

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
        val balance = transactions.sumByDouble { transaction ->
            transaction.amount
        }
        _balanceLiveData.postValue(balance.toString())
    }


}

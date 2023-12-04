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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(context: Context) : ViewModel() {

    private val userRepository: UserRepository = UserRepositoryImpl()

    private val _balanceLiveData = MutableLiveData<String>()
    val balanceLiveData: LiveData<String> get() = _balanceLiveData

    fun getTransactionsOfUser() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val transactions = userRepository.getTransactionsOfUser()
                calculateBalance(transactions)
            } catch (e: Exception) {
                handleException("Error getting user transactions", e)
            }
        }
    }

    fun saveLocalData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userRepository.getBudgets()
                userRepository.getUserPredictions()
            } catch (e: Exception) {
                handleException("Error getting budgets or user predictions", e)
            }
        }
    }

    private fun calculateBalance(transactions: List<Transaction>) {
        val balance = transactions.sumByDouble(Transaction::amount)
        _balanceLiveData.postValue(balance.toString())
    }

    private fun handleException(message: String, exception: Exception) {
        Log.d("Transactions", "$message: ${exception.message.toString()}")
    }
}

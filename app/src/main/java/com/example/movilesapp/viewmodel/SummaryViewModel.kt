package com.example.movilesapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Prediction
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import kotlinx.coroutines.launch
import java.util.Calendar

class SummaryViewModel : ViewModel() {

    private val userRepository: UserRepository = UserRepositoryImpl()

    private val _incomeLiveData = MutableLiveData<String>()
    val incomeLiveData: LiveData<String> get() = _incomeLiveData

    private val _expenseLiveData = MutableLiveData<String>()
    val expenseLiveData: LiveData<String> get() = _expenseLiveData

    private val _negativeBalanceDaysLiveData = MutableLiveData<Int>()
    val negativeBalanceDaysLiveData: LiveData<Int> get() = _negativeBalanceDaysLiveData

    private val _positiveBalanceDaysLiveData = MutableLiveData<Int>()
    val positiveBalanceDaysLiveData: LiveData<Int> get() = _positiveBalanceDaysLiveData

    private val _evenBalanceDaysLiveData = MutableLiveData<Int>()
    val evenBalanceDaysLiveData: LiveData<Int> get() = _evenBalanceDaysLiveData

    private val _allTransactionsLiveData = MutableLiveData<List<Transaction>>()
    val allTransactionsLiveData: LiveData<List<Transaction>> get() = _allTransactionsLiveData

    private val _allPredictionsLiveData = MutableLiveData<List<Prediction>>()
    val allPredictionsLiveData: LiveData<List<Prediction>> get() = _allPredictionsLiveData


    fun getPredictionsOfUser() {
        viewModelScope.launch {
            try {
                val predictions = userRepository.getUserPredictions()
                _allPredictionsLiveData.postValue(predictions)
            } catch (e: Exception) {
                Log.d("Predictions", "Error getting user predictions: ${e.message.toString()}")
            }
        }
    }


    fun getTransactionsOfUser() {
        viewModelScope.launch {
            try {
                val transactions = userRepository.getTransactionsOfUser()
                _allTransactionsLiveData.postValue(transactions)
                calculateTotalIncome(transactions)
                calculateTotalExpense(transactions)
                calculateBalanceDays(transactions)
            } catch (e: Exception) {
                Log.d("Transactions", "Error getting user transactions: ${e.message.toString()}")
            }
        }
    }

    private fun calculateTotalIncome(transactions: List<Transaction>) {
        val totalIncome = transactions
            .filter { transaction -> transaction.type == "Income" }
            .sumByDouble { transaction -> transaction.amount }

        _incomeLiveData.postValue(totalIncome.toString())
    }

    private fun calculateTotalExpense(transactions: List<Transaction>) {
        val totalIncome = transactions
            .filter { transaction -> transaction.type == "Expense" }
            .sumByDouble { transaction -> transaction.amount }

        _expenseLiveData.postValue(totalIncome.toString())
    }

    private fun calculateBalanceDays(transactions: List<Transaction>) {
        val balanceMap = mutableMapOf<String, Double>()
        val balanceDays = mutableMapOf<String, String>()
        val calendar = Calendar.getInstance()

        for (transaction in transactions) {
            val timestamp = transaction.date
            calendar.time = timestamp.toDate()
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val dateKey = "$dayOfMonth-$month-$year"

            val dailyBalance = balanceMap[dateKey] ?: 0.0

            if (transaction.type == "Income") {
                balanceMap[dateKey] = dailyBalance + transaction.amount
            } else if (transaction.type == "Expense") {
                balanceMap[dateKey] = dailyBalance + transaction.amount
            }
        }

        for ((dateKey, dailyBalance) in balanceMap) {
            val dayBalanceType = when {
                dailyBalance > 0.0 -> "positive"
                dailyBalance < 0.0 -> "negative"
                else -> "even"
            }
            balanceDays[dateKey] = dayBalanceType
        }

        val positiveDays = balanceDays.count { it.value == "positive" }
        val negativeDays = balanceDays.count { it.value == "negative" }
        val evenDays = balanceDays.count { it.value == "even" }

        _positiveBalanceDaysLiveData.postValue(positiveDays)
        _negativeBalanceDaysLiveData.postValue(negativeDays)
        _evenBalanceDaysLiveData.postValue(evenDays)
    }

}
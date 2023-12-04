package com.example.movilesapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Prediction
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class SummaryViewModel(context: Context) : ViewModel() {

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
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val predictions = userRepository.getUserPredictions()
                _allPredictionsLiveData.postValue(predictions)
            } catch (e: Exception) {
                handleException("Predictions", "Error getting user predictions", e)
            }
        }
    }

    fun getTransactionsOfUser() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val transactions = userRepository.getTransactionsOfUser()
                _allTransactionsLiveData.postValue(transactions)
                calculateTotalIncomeAndExpense(transactions)
                calculateBalanceDays(transactions)
            } catch (e: Exception) {
                handleException("Transactions", "Error getting user transactions", e)
            }
        }
    }

    private fun calculateTotalIncomeAndExpense(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.type == "Income" }.sumByDouble { it.amount }
        val totalExpense = transactions.filter { it.type == "Expense" }.sumByDouble { it.amount }

        _incomeLiveData.postValue(totalIncome.toString())
        _expenseLiveData.postValue(totalExpense.toString())
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

            balanceMap[dateKey] = dailyBalance + transaction.amount * if (transaction.type == "Income") 1 else -1
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

    private fun handleException(tag: String, message: String, exception: Exception) {
        Log.d(tag, "$message: ${exception.message.toString()}")
    }
}

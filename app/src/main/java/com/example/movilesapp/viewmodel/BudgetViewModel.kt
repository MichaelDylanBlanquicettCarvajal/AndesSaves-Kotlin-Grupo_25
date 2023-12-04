package com.example.movilesapp.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.UserSingleton
import com.example.movilesapp.model.entities.Budget
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BudgetViewModel(private val context: Context) : ViewModel() {

    private val userRepository: UserRepository = UserRepositoryImpl()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

    private val _loadingMessageLiveData = MutableLiveData<String>()
    val loadingMessageLiveData: LiveData<String> get() = _loadingMessageLiveData

    private val _budgetsLiveData = MutableLiveData<List<Budget>>()
    val budgetsLiveData: LiveData<List<Budget>> get() = _budgetsLiveData

    fun createBudget(
        name: String,
        total: Double,
        type: Int,
        date: Timestamp,
        onBudgetCreated: (Boolean) -> Unit
    ) {
        val userSingleton = UserSingleton.getUserInfoSingleton()
        val userId = userSingleton?.userId ?: ""

        _errorMessageLiveData.value = ""

        if (name.isEmpty() || total == 0.0 || total < 0.0) {
            handleInvalidInput("Name is empty or Amount is not a valid number", onBudgetCreated)
            return
        }

        if (date.seconds < Timestamp.now().seconds) {
            handleInvalidInput("Please select a future date", onBudgetCreated)
            return
        }

        val budget = Budget(userId, name, 0.0, total, type, date)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                setLoading(true)
                val isSuccess = userRepository.createBudget(budget)
                handleOperationResult(isSuccess, onBudgetCreated)
            } catch (e: Exception) {
                handleException(e, onBudgetCreated)
            } finally {
                setLoading(false)
            }
        }
    }

    fun getBudgets() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (isNetworkAvailable()) {
                    syncBudgetData()
                }

                val budgets = userRepository.getBudgets()
                val sortedBudgets =
                    budgets.sortedWith(compareBy<Budget> { it.type }.thenBy { it.date })
                _budgetsLiveData.value = sortedBudgets
                _loadingMessageLiveData.value = "Budgets"
            } catch (e: Exception) {
                _errorMessageLiveData.value = "Error getting budgets: ${e.message.toString()}"
            }
        }
    }

    fun updateBudgetContributions(
        budget: Budget,
        newContributions: Double,
        onBudgetModified: (Boolean) -> Unit
    ) {
        val totalContributions = budget.contributions + newContributions

        if (totalContributions > budget.total || newContributions == 0.0 || newContributions < 0.0) {
            handleInvalidInput("Invalid contribution amount", onBudgetModified)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSuccess = userRepository.updateBudgetContributions(
                    budget.budgetId,
                    totalContributions
                )
                handleOperationResult(isSuccess, onBudgetModified)
            } catch (e: Exception) {
                handleException(e, onBudgetModified)
            }
        }
    }

    fun deleteBudgetById(budgetId: String, onBudgetDeleted: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSuccess = userRepository.deleteBudgetById(budgetId)
                handleOperationResult(isSuccess, onBudgetDeleted)
            } catch (e: Exception) {
                handleException(e, onBudgetDeleted)
            }
        }
    }

    fun resetErrorMessage() {
        _errorMessageLiveData.value = ""
    }

    private fun handleInvalidInput(message: String, callback: (Boolean) -> Unit) {
        _errorMessageLiveData.value = message
        callback(false)
    }

    private fun handleOperationResult(isSuccess: Boolean, callback: (Boolean) -> Unit) {
        if (isSuccess) {
            callback(true)
        } else {
            _errorMessageLiveData.value = "Error during operation"
            callback(false)
        }
    }

    private fun handleException(e: Exception, callback: (Boolean) -> Unit) {
        _errorMessageLiveData.value = "Exception ${e.message.toString()}"
        callback(false)
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private suspend fun syncBudgetData() {
        _loadingMessageLiveData.value = "Loading..."
        userRepository.syncDeleteBudgetsFirebase()
        userRepository.syncBudgetsFirebase()
        userRepository.syncUpdateBudgetsFirebase()
    }
}

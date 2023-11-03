package com.example.movilesapp.viewmodel

import android.content.Context
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
import kotlinx.coroutines.launch

class BudgetViewModel(context: Context) : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl(context)

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

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

        if (name.isEmpty()) {
            _errorMessageLiveData.value = "Name is empty"
            onBudgetCreated(false)
            return
        }

        if (total == 0.0) {
            _errorMessageLiveData.value = "Amount is Empty"
            onBudgetCreated(false)
            return
        }

        if (total < 0.0) {
            _errorMessageLiveData.value = "Amount is not a Valid Number"
            onBudgetCreated(false)
            return
        }

        val currentTime = Timestamp.now()

        if (date.seconds < currentTime.seconds) {
            _errorMessageLiveData.value = "Please select a Future Date"
            onBudgetCreated(false)
            return
        }

        val budget = Budget(userId, name, 0.0, total, type, date)

        viewModelScope.launch {
            try {
                setLoading(true)
                val isSuccess = userRepository.createBudget(budget)
                if (isSuccess) {
                    onBudgetCreated(true) // Llamar el callback con true en caso de éxito
                } else {
                    Log.d("Budget", "Error al crear el presupuesto")
                    onBudgetCreated(false) // Llamar el callback con false en caso de error
                }
            } catch (e: Exception) {
                Log.d("Budget", "Exception ${e.message.toString()}")
                onBudgetCreated(false) // Llamar el callback con false en caso de excepción
            } finally {
                setLoading(false)
            }
        }
    }

    fun getBudgets() {
        viewModelScope.launch {
            try {
                val budgets = userRepository.getBudgets()
                val sortedBudgets = budgets.sortedWith(compareBy<Budget> { it.type }.thenBy { it.date })
                _budgetsLiveData.value = sortedBudgets
            } catch (e: Exception) {
                _errorMessageLiveData.value = "Error getting budgets of type 0: ${e.message.toString()}"
            }
        }
    }

    fun updateBudgetContributions(budget: Budget, newContributions: Double, onBudgetModified: (Boolean) -> Unit) {
        val totalContributions = budget.contributions + newContributions

        if (totalContributions > budget.total) {
            _errorMessageLiveData.value = "Total contributions cannot exceed the total budget amount."
            onBudgetModified(false)
            return
        } else if (newContributions == 0.0) {
            _errorMessageLiveData.value = "Amount is empty or 0"
            onBudgetModified(false)
            return
        } else if (newContributions < 0.0) {
            _errorMessageLiveData.value = "Amount is not a Valid Number"
            onBudgetModified(false)
            return
        } else {
            viewModelScope.launch {
                try {
                    val isSuccess = userRepository.updateBudgetContributions(budget.budgetId, totalContributions)
                    if (isSuccess) {
                        onBudgetModified(true)
                    } else {
                        _errorMessageLiveData.value = "Error updating budget contributions."
                        onBudgetModified(false)
                    }
                } catch (e: Exception) {
                    _errorMessageLiveData.value = "Error updating budget contributions: ${e.message.toString()}"
                    onBudgetModified(false)
                }
            }
        }
    }


    fun deleteBudgetById(budgetId: String, onBudgetDeleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isSuccess = userRepository.deleteBudgetById(budgetId)
                if (isSuccess) {
                    onBudgetDeleted(true)
                } else {
                    _errorMessageLiveData.value = "Error deleting budget."
                }
            } catch (e: Exception) {
                _errorMessageLiveData.value = "Error deleting budget: ${e.message.toString()}"
            }
        }
    }

    fun resetErrorMessage(){
        _errorMessageLiveData.value = ""
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}

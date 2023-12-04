package com.example.movilesapp.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(private val context: Context) : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl(context)

    private val _transactionsLiveData = MutableLiveData<List<Transaction>>()
    val transactionsLiveData: LiveData<List<Transaction>> get() = _transactionsLiveData

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

    private val _loadingMessageLiveData = MutableLiveData<String>()
    val loadingMessageLiveData: LiveData<String> get() = _loadingMessageLiveData

    fun getTransactionsOfUser() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (isNetworkAvailable()){
                    _loadingMessageLiveData.value = "Loading..."
                    userRepository.syncTransactionsFirebase()
                }
                val transactions = userRepository.getTransactionsOfUser()
                _transactionsLiveData.value = transactions
                _loadingMessageLiveData.value = "History"
            } catch (e: Exception) {
                _errorMessageLiveData.value = "Error getting user transactions: ${e.message.toString()}"
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

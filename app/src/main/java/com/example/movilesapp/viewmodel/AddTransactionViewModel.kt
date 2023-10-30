import android.net.Uri
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import com.example.movilesapp.model.repositories.implementations.UserRepositoryImpl
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class AddTransactionViewModel() : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessageLiveData

    fun createTransaction(
        name: String,
        amount: String,
        type: String,
        category: String,
        imageUri: String?,
        onHomeSuccess: () -> Unit
    ) {
        _errorMessageLiveData.value = ""
        if (name.isEmpty() || amount.isEmpty()) {
            _errorMessageLiveData.value = "Name or Amount is empty"
            return
        }

        val amountDouble = if (type == "Expense") {
            -amount.toDouble()
        } else {
            amount.toDouble()
        }

        val transaction = Transaction(
            transactionId = "",
            name = name,
            amount = amountDouble,
            type = type,
            category = category,
            date = Timestamp.now(),
            imageUri = imageUri?: ""
        )

        viewModelScope.launch {
            try {
                setLoading(true)
                val isSuccess = userRepository.createTransaction(transaction)
                if (isSuccess) {
                    withContext(Dispatchers.Main) {
                        onHomeSuccess()
                    }
                } else {
                    Log.d("Transaction", "Error al crear la transacción")
                }
            } catch (e: Exception) {
                Log.d("Transaction", "Exception ${e.message.toString()}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}

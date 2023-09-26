import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTransactionViewModel() : ViewModel() {
    private val userRepository = UserRepository()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    fun createTransaction(
        name: String,
        amount: Double,
        source: String,
        type: String,
        category: String,
        onHomeSuccess: () -> Unit
    ) {
        val transaction = Transaction(
            transactionId = "",
            name = name,
            amount = amount,
            source = source,
            type = type,
            category = category
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

package com.example.movilesapp.view

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityHistoryBinding
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.GenericViewModelFactory
import com.example.movilesapp.viewmodel.HistoryViewModel
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import java.text.NumberFormat

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this, GenericViewModelFactory(this)).get(HistoryViewModel::class.java)

        setupBackButton()
        observeViewModel()

        ThemeUtils.checkAndSetNightMode(this)
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.transactionsLiveData.observe(this) { transactions ->
            clearLinearLayout()
            createTransactionViews(transactions)
        }

        viewModel.loadingMessageLiveData.observe(this) { loadingMessage ->
            binding.textViewTitle.text = loadingMessage
        }

        viewModel.getTransactionsOfUser()
    }

    private fun clearLinearLayout() {
        binding.linearLayoutContainer.removeAllViews()
    }

    private fun createTransactionViews(transactions: List<Transaction>) {
        val numberFormat = NumberFormat.getNumberInstance()
        for (transaction in transactions) {
            val transactionLinearLayout = createTransactionLinearLayout(transaction)
            binding.linearLayoutContainer.addView(transactionLinearLayout)
        }
    }

    private fun createTransactionLinearLayout(transaction: Transaction): LinearLayout {
        val transactionLinearLayout = LinearLayout(this)
        transactionLinearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        transactionLinearLayout.orientation = LinearLayout.HORIZONTAL
        transactionLinearLayout.gravity = Gravity.CENTER_VERTICAL
        transactionLinearLayout.id = View.generateViewId()

        transactionLinearLayout.setOnClickListener {
            openDialog(transaction)
        }

        val symbolTextView = createSymbolTextView(transaction.type)
        transactionLinearLayout.addView(symbolTextView)

        val innerLinearLayout = createInnerLinearLayout(transaction)
        transactionLinearLayout.addView(innerLinearLayout)

        val amountTextView = createAmountTextView(transaction)
        transactionLinearLayout.addView(amountTextView)

        return transactionLinearLayout
    }

    private fun createSymbolTextView(type: String): TextView {
        val symbolTextView = TextView(this)
        symbolTextView.text = when (type) {
            "Income" -> "+"
            "Expense" -> "-"
            else -> "?"
        }
        symbolTextView.textSize = 48f
        symbolTextView.setTextColor(
            ContextCompat.getColor(
                this,
                when (type) {
                    "Income" -> R.color.green
                    "Expense" -> R.color.red
                    else -> R.color.black
                }
            )
        )
        symbolTextView.gravity = Gravity.CENTER_VERTICAL
        symbolTextView.setTypeface(null, Typeface.BOLD)
        val innerLinearLayoutParams = LinearLayout.LayoutParams(
            40.dpToPx(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        innerLinearLayoutParams.gravity = Gravity.CENTER
        innerLinearLayoutParams.leftMargin = 20.dpToPx()
        symbolTextView.layoutParams = innerLinearLayoutParams
        return symbolTextView
    }

    private fun createInnerLinearLayout(transaction: Transaction): LinearLayout {
        val innerLinearLayout = LinearLayout(this)
        innerLinearLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        innerLinearLayout.orientation = LinearLayout.VERTICAL

        val nameTextView = TextView(this)
        nameTextView.text = transaction.name
        nameTextView.textSize = 18f

        val dateTextView = TextView(this)
        dateTextView.text = transaction.date.toDate().toString()
        dateTextView.textSize = 12f
        dateTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))

        innerLinearLayout.addView(nameTextView)
        innerLinearLayout.addView(dateTextView)
        return innerLinearLayout
    }

    private fun createAmountTextView(transaction: Transaction): TextView {
        val amountTextView = TextView(this)
        val formattedAmount = formatAmountAsCurrency(transaction.amount)
        amountTextView.text = "$ $formattedAmount"
        amountTextView.textSize = 18f
        amountTextView.gravity = Gravity.CENTER_VERTICAL
        return amountTextView
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun openDialog(transaction: Transaction) {
        // ... (unchanged)
    }

    private fun formatAmountAsCurrency(amount: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance()
        var formattedAmount = numberFormat.format(amount)
        if (formattedAmount.endsWith(".00")) {
            formattedAmount = formattedAmount.substring(0, formattedAmount.length - 3)
        }
        return formattedAmount
    }

    private fun formatTimestampAsDate(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        return DateUtils.formatDateTime(this, date.time, DateUtils.FORMAT_SHOW_DATE)
    }
}

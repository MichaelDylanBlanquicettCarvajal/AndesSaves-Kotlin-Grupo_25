package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.setMargins
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityHistoryBinding
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.viewmodel.HistoryViewModel
import java.text.NumberFormat

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.transactionsLiveData.observe(this) { transactions ->
            clearLinearLayout()
            createTransactionViews(transactions)
        }

        viewModel.getTransactionsOfUser()
    }

    private fun clearLinearLayout() {
        binding.linearLayoutContainer.removeAllViews()
    }

    private fun createTransactionViews(transactions: List<Transaction>) {
        val numberFormat = NumberFormat.getNumberInstance()
        for (transaction in transactions) {
            val transactionLinearLayout = LinearLayout(this)
            transactionLinearLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            transactionLinearLayout.orientation = LinearLayout.HORIZONTAL
            transactionLinearLayout.gravity = Gravity.CENTER_VERTICAL

            val symbolTextView = when (transaction.type) {
                "Income" -> {
                    val plusTextView = TextView(this)
                    plusTextView.text = "+"
                    plusTextView.textSize = 48f
                    plusTextView.setTextColor(resources.getColor(R.color.green))
                    plusTextView.gravity = Gravity.CENTER_VERTICAL
                    plusTextView.setTypeface(null, Typeface.BOLD)
                    val innerLinearLayoutParams = LinearLayout.LayoutParams(
                        40.dpToPx(),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    innerLinearLayoutParams.gravity = Gravity.CENTER
                    innerLinearLayoutParams.leftMargin = 20.dpToPx()
                    plusTextView.layoutParams = innerLinearLayoutParams
                    plusTextView
                }
                "Expense" -> {
                    val minusTextView = TextView(this)
                    minusTextView.text = "-"
                    minusTextView.textSize = 48f
                    minusTextView.setTextColor(resources.getColor(R.color.red))
                    minusTextView.gravity = Gravity.CENTER_VERTICAL
                    minusTextView.setTypeface(null, Typeface.BOLD)
                    val innerLinearLayoutParams = LinearLayout.LayoutParams(
                        40.dpToPx(),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    innerLinearLayoutParams.gravity = Gravity.CENTER
                    innerLinearLayoutParams.leftMargin = 20.dpToPx()
                    minusTextView.layoutParams = innerLinearLayoutParams
                    minusTextView
                }
                else -> {
                    val defaultTextView = TextView(this)
                    defaultTextView.text = "?"
                    defaultTextView.textSize = 48f
                    defaultTextView.setTextColor(resources.getColor(R.color.black))
                    defaultTextView.gravity = Gravity.CENTER_VERTICAL
                    defaultTextView.setTypeface(null, Typeface.BOLD)
                    val innerLinearLayoutParams = LinearLayout.LayoutParams(
                        40.dpToPx(),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    innerLinearLayoutParams.gravity = Gravity.CENTER
                    innerLinearLayoutParams.leftMargin = 20.dpToPx()
                    defaultTextView.layoutParams = innerLinearLayoutParams
                    defaultTextView
                }
            }

            val innerLinearLayout = LinearLayout(this)
            innerLinearLayout.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            innerLinearLayout.orientation = LinearLayout.VERTICAL

            val nameTextView = TextView(this)
            nameTextView.text = transaction.name
            nameTextView.textSize = 16f

            val dateTextView = TextView(this)
            dateTextView.text = transaction.date
            dateTextView.textSize = 12f
            dateTextView.setTextColor(resources.getColor(R.color.gray))

            innerLinearLayout.addView(nameTextView)
            innerLinearLayout.addView(dateTextView)

            transactionLinearLayout.addView(symbolTextView)
            transactionLinearLayout.addView(innerLinearLayout)

            val amountTextView = TextView(this)
            val formattedAmount = numberFormat.format(transaction.amount)
            amountTextView.text = "$$formattedAmount"
            amountTextView.textSize = 18f
            amountTextView.gravity = Gravity.CENTER_VERTICAL

            transactionLinearLayout.addView(amountTextView)

            binding.linearLayoutContainer.addView(transactionLinearLayout)
        }
    }


    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }


}

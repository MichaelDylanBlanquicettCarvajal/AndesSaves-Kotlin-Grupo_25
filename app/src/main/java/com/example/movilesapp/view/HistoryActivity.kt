package com.example.movilesapp.view

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityHistoryBinding
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.HistoryViewModel
import com.google.firebase.Timestamp
import java.text.NumberFormat
import com.google.firebase.storage.FirebaseStorage

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        setupBackButton()
        observeViewModel()

        ThemeUtils.checkAndSetNightMode(this)

    }

    private fun setupBackButton() {
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
            transactionLinearLayout.id = View.generateViewId()

            transactionLinearLayout.setOnClickListener {

                openDialog(transaction)
            }

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
            nameTextView.textSize = 18f

            val dateTextView = TextView(this)
            dateTextView.text = transaction.date.toDate().toString()
            dateTextView.textSize = 12f
            dateTextView.setTextColor(resources.getColor(R.color.gray))

            innerLinearLayout.addView(nameTextView)
            innerLinearLayout.addView(dateTextView)

            transactionLinearLayout.addView(symbolTextView)
            transactionLinearLayout.addView(innerLinearLayout)

            val amountTextView = TextView(this)
            val formattedAmount = numberFormat.format(transaction.amount)
            amountTextView.text = "$ $formattedAmount"
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



    private fun openDialog(transaction: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_detail_history, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        val transactionNameTextView = dialogView.findViewById<TextView>(R.id.TransactionName)
        val transactionAmountTextView = dialogView.findViewById<TextView>(R.id.TransactionAmount)
        val transactionDateTextView = dialogView.findViewById<TextView>(R.id.TransactionDate)
        val transactionImageView = dialogView.findViewById<ImageView>(R.id.transactionImageView)

        val formattedAmount = formatAmountAsCurrency(transaction.amount)
        transactionAmountTextView.text = formattedAmount

        if (transaction.type == "Income") {
            transactionAmountTextView.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            transactionAmountTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
        }

        val formattedDate = formatTimestampAsDate(transaction.date)
        transactionDateTextView.text = formattedDate

        transactionNameTextView.text = transaction.name

        val transactionId = transaction.transactionId
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("Transactions/$transactionId.jpg")

        imageRef.downloadUrl.addOnSuccessListener { uri ->

            Glide.with(this /* context */)
                .load(uri)
                .into(transactionImageView)
        }

        dialog.show()
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

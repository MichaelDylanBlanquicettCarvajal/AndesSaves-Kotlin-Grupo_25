package com.example.movilesapp.view

import AddTransactionViewModel
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityAddTransactionBinding
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.RegisterViewModel

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: AddTransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(AddTransactionViewModel::class.java)

        binding.LayoutExpenseCategory.visibility = View.GONE
        setupBackButton()
        setupErrorMessageObserver()
        setupToggleButtonTypeListeners()
        setupToggleButtonExCategoryListeners()
        setupAddTransactionButton()

        ThemeUtils.checkAndSetNightMode(this)
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupErrorMessageObserver() {
        viewModel.errorMessageLiveData.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                binding.textViewErrorMessage.text = errorMessage
            } else {
                binding.textViewErrorMessage.text = ""
            }
        }
    }

    private fun setupToggleButtonTypeListeners() {
        binding.toggleButtonIncome.setOnClickListener {
            if (!binding.toggleButtonIncome.isChecked) {
                binding.toggleButtonIncome.isChecked = true
            }
            binding.toggleButtonExpense.isChecked = false
            binding.LayoutExpenseCategory.visibility = View.GONE

            binding.textViewTitle.text = "INCOME"
            binding.buttonAddTransaction.setBackgroundColor(getColor(R.color.green))
            binding.RelativeLayout.setBackgroundColor(getColor(R.color.green))
            window.statusBarColor = getColor(R.color.green)

            viewModel.loading.observe(this) { isLoading ->
                binding.buttonAddTransaction.isEnabled = !isLoading
                binding.buttonAddTransaction.text = if (isLoading) "Loading..." else "ADD INCOME"
            }
        }

        binding.toggleButtonExpense.setOnClickListener {
            if (!binding.toggleButtonExpense.isChecked) {
                binding.toggleButtonExpense.isChecked = true
            }
            binding.toggleButtonIncome.isChecked = false
            binding.LayoutExpenseCategory.visibility = View.VISIBLE

            binding.textViewTitle.text = "EXPENSE"
            binding.buttonAddTransaction.setBackgroundColor(getColor(R.color.red))
            binding.RelativeLayout.setBackgroundColor(getColor(R.color.red))
            window.statusBarColor = getColor(R.color.red)

            viewModel.loading.observe(this) { isLoading ->
                binding.buttonAddTransaction.isEnabled = !isLoading
                binding.buttonAddTransaction.text = if (isLoading) "Loading..." else "ADD EXPENSE"
            }
        }
    }

    private fun setupToggleButtonExCategoryListeners() {
        val toggleButtons = arrayOf(
            binding.toggleButtonFood,
            binding.toggleButtonTransport,
            binding.toggleButtonHouse,
            binding.toggleButtonOther
        )

        toggleButtons.forEach { button ->
            button.setOnClickListener {
                toggleButtons.forEach { otherButton ->
                    if (otherButton != button) {
                        otherButton.isChecked = false
                    }
                }
            }
        }
    }

    private fun setupAddTransactionButton() {
        binding.buttonAddTransaction.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val amount = binding.editTextAmount.text.toString()
            val source = binding.editTextSource.text.toString()

            val type = if (binding.toggleButtonIncome.isChecked) "Income" else "Expense"
            val category = if (type == "Income") "Income" else getCategoryValue()

            viewModel.createTransaction(name, amount, source, type, category) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun getCategoryValue(): String {
        return when {
            binding.toggleButtonFood.isChecked -> "Food"
            binding.toggleButtonTransport.isChecked -> "Transport"
            binding.toggleButtonHouse.isChecked -> "House"
            else -> "Other"
        }
    }

}
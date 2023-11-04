package com.example.movilesapp.view

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.ToggleButton
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityBudgetBinding
import com.example.movilesapp.model.entities.Budget
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.BudgetViewModel
import com.example.movilesapp.viewmodel.GenericViewModelFactory
import com.example.movilesapp.viewmodel.HistoryViewModel
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar


class BudgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBudgetBinding
    private lateinit var viewModel: BudgetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this, GenericViewModelFactory(this)).get(BudgetViewModel::class.java)

        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        ThemeUtils.checkAndSetNightMode(this)

        observeViewModel()
        setUpCreateBudgetButton()
    }

    private fun observeViewModel() {
        viewModel.budgetsLiveData.observe(this) { budgets ->
            clearLinearLayout()
            createBudgetViews(budgets)
        }

        viewModel.loadingMessageLiveData.observe(this) { loadingMessage ->
            binding.textViewTitle.text = loadingMessage
        }

        viewModel.getBudgets()
    }


    private fun clearLinearLayout() {
        binding.linearLayoutContainer.removeAllViews()
    }

    private fun createBudgetViews(budgets: List<Budget>) {
        val numberFormat = NumberFormat.getNumberInstance()
        val container = binding.linearLayoutContainer

        var hasIndividualBudgets = false
        var hasGroupBudgets = false

        for (budget in budgets) {
            val type = budget.type

            val budgetLinearLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16.dpToPx())
            budgetLinearLayout.layoutParams = params
            budgetLinearLayout.orientation = LinearLayout.HORIZONTAL
            budgetLinearLayout.gravity = Gravity.CENTER_VERTICAL

            val nameDateLayout = LinearLayout(this)
            nameDateLayout.layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            nameDateLayout.orientation = LinearLayout.VERTICAL

            val nameTextView = TextView(this)
            nameTextView.text = budget.name
            nameTextView.textSize = 20f
            nameTextView.setTypeface(null, Typeface.BOLD)

            val dateTextView = TextView(this)
            dateTextView.text = formatDate(budget.date)
            dateTextView.textSize = 13f
            dateTextView.setTextColor(resources.getColor(R.color.gray))

            nameDateLayout.addView(nameTextView)
            nameDateLayout.addView(dateTextView)

            val totalTextView = TextView(this)
            val formattedTotal = "$ " + numberFormat.format(budget.total)
            totalTextView.text = formattedTotal
            totalTextView.textSize = 18f
            totalTextView.gravity = Gravity.END or Gravity.CENTER_VERTICAL

            budgetLinearLayout.setOnClickListener {
                showBudgetDetailsDialog(budget)
            }

            if (type == 0) {
                if (!hasIndividualBudgets) {
                    val individualTitle = createTitleTextView("Individual")
                    container.addView(individualTitle)
                    hasIndividualBudgets = true
                }
                budgetLinearLayout.addView(nameDateLayout)
                budgetLinearLayout.addView(totalTextView)
                container.addView(budgetLinearLayout)
            } else if (type == 1) {
                if (!hasGroupBudgets) {
                    val groupTitle = createTitleTextView("Group")
                    container.addView(groupTitle)
                    hasGroupBudgets = true
                }
                budgetLinearLayout.addView(nameDateLayout)
                budgetLinearLayout.addView(totalTextView)
                container.addView(budgetLinearLayout)
            }
        }

        val space = Space(this)
        space.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 100.dpToPx()
        )
        container.addView(space)
    }

    private fun createTitleTextView(title: String): TextView {
        val titleTextView = TextView(this)
        titleTextView.text = title
        titleTextView.textSize = 26f
        titleTextView.setTypeface(null, Typeface.BOLD)
        titleTextView.setPadding(0, 16.dpToPx(), 0, 16.dpToPx())
        return titleTextView
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun formatDate(date: Timestamp): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        return sdf.format(date.toDate())
    }

    private fun showBudgetDetailsDialog(budget: Budget) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_detail_budget, null)
        viewModel.resetErrorMessage()

        // Cambiar el texto de TextViewName con el valor de budget.name
        val nameTextView = dialogView.findViewById<TextView>(R.id.TextViewName)
        nameTextView.text = budget.name

        // Cambiar el texto de TextViewDate con el valor de budget.date
        val dateTextView = dialogView.findViewById<TextView>(R.id.TextViewDate)
        dateTextView.text = formatDate(budget.date)

        // Calcular el porcentaje
        val contributions = budget.contributions
        val total = budget.total

        val percentage = if (total > 0.0) {
            val percentageValue = (contributions / total) * 100
            String.format("%.1f%%", percentageValue)
        } else {
            "0%" // O un valor predeterminado si el total es cero o negativo
        }

        // Mostrar el porcentaje en el TextView
        val percentageTextView = dialogView.findViewById<TextView>(R.id.textViewPercentage)
        percentageTextView.text = percentage


        // Formatear contributions y total como valores de dinero y mostrarlos en TextViewAvailable
        val availableTextView = dialogView.findViewById<TextView>(R.id.TextViewAvailable)

        val contributionsFormatted = formatAsCurrency(budget.contributions)
        val totalFormatted = formatAsCurrency(budget.total)

        availableTextView.text = "$contributionsFormatted / $totalFormatted"

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        setupAddContributionButton(dialogView, dialog, budget)
        setupDeleteBudgetButton(dialogView, dialog, budget)
        setupErrorMessageObserver(dialogView)

        dialog.show()
    }

    private fun setupDeleteBudgetButton(dialogView: View, dialog: AlertDialog, budget: Budget) {
        val buttonDeleteBudget = dialogView.findViewById<Button>(R.id.buttonDeleteBudget)
        buttonDeleteBudget.setOnClickListener {
            viewModel.deleteBudgetById(budget.budgetId) { isSuccess ->
                if (isSuccess) {
                    viewModel.getBudgets()
                    dialog.dismiss()
                }

            }
        }
    }

    private fun setupAddContributionButton(dialogView: View, dialog: AlertDialog, budget: Budget) {
        val buttonAddContribution = dialogView.findViewById<Button>(R.id.buttonAddContribution)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.editTextAmount)

        buttonAddContribution.setOnClickListener {
            var contributionAmount = 0.0
            if (editTextAmount.text.isNotEmpty()) {
                if(editTextAmount.text.toString().toDoubleOrNull() != null){
                    contributionAmount = editTextAmount.text.toString().toDouble()
                }
                else{
                    contributionAmount = -1.0
                }
            }

            viewModel.updateBudgetContributions(budget, contributionAmount) { isSuccess ->
                if (isSuccess) {
                    viewModel.getBudgets()
                    dialog.dismiss()
                }

            }
        }
    }

    private fun formatAsCurrency(amount: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance()

        var formattedAmount = numberFormat.format(amount)

        if (formattedAmount.endsWith(".00")) {
            formattedAmount = formattedAmount.substring(0, formattedAmount.length - 3)
        }

        return formattedAmount
    }


    private fun setUpCreateBudgetButton() {
        binding.btnCreateBudget.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_create_budget, null)
            viewModel.resetErrorMessage()

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            val dialog = builder.create()

            setupErrorMessageObserver(dialogView)
            setupToggleButtonListeners(dialogView)
            setupCreateBudgetButtonListeners(dialogView, dialog)

            dialog.show()
        }
    }

    private fun setupErrorMessageObserver(dialogView: View) {
        val textViewErrorMessage = dialogView.findViewById<TextView>(R.id.textViewErrorMessage)

        viewModel.errorMessageLiveData.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                textViewErrorMessage.text = errorMessage
            } else {
                textViewErrorMessage.text = ""
            }
        }
    }

    private fun setupToggleButtonListeners(dialogView: View) {
        val toggleButtonIndividual =
            dialogView.findViewById<ToggleButton>(R.id.toggleButtonIndividual)
        val toggleButtonGroup = dialogView.findViewById<ToggleButton>(R.id.toggleButtonGroup)

        val toggleButtons = arrayOf(toggleButtonIndividual, toggleButtonGroup)

        toggleButtons.forEach { button ->
            button.setOnClickListener {
                toggleButtons.forEach { otherButton ->
                    if (otherButton != button) {
                        otherButton.isChecked = false
                    }
                }
                if (!button.isChecked) {
                    button.isChecked = true
                }
            }
        }
    }

    private fun setupCreateBudgetButtonListeners(dialogView: View, dialog: AlertDialog) {
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.editTextAmount)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val buttonCreateBudget = dialogView.findViewById<Button>(R.id.buttonCreateBudget)
        val toggleButtonGroup = dialogView.findViewById<ToggleButton>(R.id.toggleButtonGroup)

        var toggleButtonSelectedValue = 0

        setupToggleButtonListeners(dialogView)

        buttonCreateBudget.setOnClickListener {


            val name = if (editTextName.text.isNotEmpty()) {
                editTextName.text.toString()
            } else {
                ""
            }

            var amount = 0.0
            if (editTextAmount.text.isNotEmpty()) {
                if (editTextAmount.text.toString().toDoubleOrNull() != null) {
                    amount = editTextAmount.text.toString().toDouble()
                } else {
                    amount = -1.0
                }
            }

            val day = datePicker.dayOfMonth
            val month = datePicker.month
            val year = datePicker.year

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)

            val selectedDate = calendar.time
            val timestamp = Timestamp(selectedDate)

            if (toggleButtonGroup.isChecked) {
                toggleButtonSelectedValue = 1
            }

            viewModel.createBudget(
                name = name,
                total = amount,
                type = toggleButtonSelectedValue,
                date = timestamp
            ) { isSuccess ->
                if (isSuccess) {
                    viewModel.getBudgets()
                    dialog.dismiss()
                }

            }
        }
    }

}
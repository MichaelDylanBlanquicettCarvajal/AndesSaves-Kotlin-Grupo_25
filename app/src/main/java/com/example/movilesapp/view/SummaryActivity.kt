package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivitySummaryBinding
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.SummaryViewModel
import java.text.NumberFormat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

class SummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummaryBinding
    private lateinit var viewModel: SummaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(SummaryViewModel::class.java)

        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        ThemeUtils.checkAndSetNightMode(this)

        viewModel.getTransactionsOfUser()
        viewModel.getPredictionsOfUser()
        setupIncomeObserver()
        setupExpenseObserver()
        setupDaysObservers()
        setupBarChart()
        observePredictions()
    }

    private fun observePredictions() {
        viewModel.allPredictionsLiveData.observe(this) { predictions ->
            if (predictions.isNotEmpty()) {
                val mostRecentPrediction = predictions.maxBy { prediction ->
                    prediction.year * 100 + prediction.month
                }

                if (mostRecentPrediction != null) {
                    val formattedPrediction = formatAsMoney(mostRecentPrediction.predicted_expense)
                    val text = "\$$formattedPrediction"
                    binding.TextExpensesPrediction.text = text
                }
            } else {
                binding.TextExpensesPrediction.text = "At least 2 months of transactions are required for predictions"
            }
        }
    }

    private fun formatAsMoney(value: Double): String {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(value)
    }



    private fun setupIncomeObserver() {
        viewModel.incomeLiveData.observe(this) { income ->
            val numberFormat = NumberFormat.getNumberInstance()
            try {
                val incomeValue = income.toDouble()
                val formattedIncome = numberFormat.format(incomeValue)
                binding.TextIncomesTotal.text = "$ $formattedIncome COP"

            } catch (e: NumberFormatException) {
                binding.TextIncomesTotal.text = "$0.0 COP"
            }
        }
    }

    private fun setupExpenseObserver() {
        viewModel.expenseLiveData.observe(this) { expense ->
            val numberFormat = NumberFormat.getNumberInstance()
            try {
                val expenseValue = expense.toDouble()
                val formattedExpense = numberFormat.format(expenseValue)
                binding.TextExpensesTotal.text = "$ $formattedExpense COP"

            } catch (e: NumberFormatException) {
                binding.TextExpensesTotal.text = "$0.0 COP"
            }
        }
    }

    private fun setupDaysObservers() {
        viewModel.positiveBalanceDaysLiveData.observe(this) { positiveDays ->
            binding.TextPositiveDaysTotal.text = "$positiveDays days"
        }

        viewModel.negativeBalanceDaysLiveData.observe(this) { negativeDays ->
            binding.TextNegativeDaysTotal.text = "$negativeDays days"
        }

        viewModel.evenBalanceDaysLiveData.observe(this) { evenDays ->
            binding.TextEvenDaysTotal.text = "$evenDays days"
        }
    }

    private fun setupBarChart() {
        viewModel.allTransactionsLiveData.observe(this) { transactions ->
            val barChart = binding.barChart

            // Paso 2: Filtrar únicamente los expenses con transaction.type = Expense
            val expenseTransactions = transactions.filter { it.type == "Expense" }

            // Paso 3: Sumar los gastos por categoría
            var foodValue = expenseTransactions.filter { it.category == "Food" }
                .sumByDouble { it.amount.toDouble() }.toFloat()

            var transportValue = expenseTransactions.filter { it.category == "Transport" }
                .sumByDouble { it.amount.toDouble() }.toFloat()

            var houseValue = expenseTransactions.filter { it.category == "House" }
                .sumByDouble { it.amount.toDouble() }.toFloat()

            var otherValue = expenseTransactions.filter { it.category == "Other" }
                .sumByDouble { it.amount.toDouble() }.toFloat()

            // Paso 4: Multiplicar por -1 para convertir los valores en positivo
            foodValue *= -1
            transportValue *= -1
            houseValue *= -1
            otherValue *= -1

            val entries = ArrayList<BarEntry>()
            entries.add(BarEntry(0f, foodValue))
            entries.add(BarEntry(1f, transportValue))
            entries.add(BarEntry(2f, houseValue))
            entries.add(BarEntry(3f, otherValue))

            val dataSet = BarDataSet(entries, "")
            dataSet.setDrawValues(true) // Activar visualización de valores
            dataSet.valueTextSize = 12f

            dataSet.colors = ColorTemplate.JOYFUL_COLORS.asList()

            val labels = ArrayList<String>()
            labels.add("Food")
            labels.add("Transport")
            labels.add("House")
            labels.add("Other")

            val data = BarData(dataSet)
            data.barWidth = 0.9f

            barChart.data = data
            barChart.setFitBars(true)
            barChart.description.isEnabled = false

            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            barChart.xAxis.granularity = 1f
            barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            barChart.xAxis.setDrawGridLines(false)
            barChart.xAxis.setDrawAxisLine(false)
            barChart.animateY(1000)
            barChart.invalidate()
        }
    }



}
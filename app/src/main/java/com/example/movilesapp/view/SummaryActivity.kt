package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivitySummaryBinding
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.GenericViewModelFactory
import com.example.movilesapp.viewmodel.SummaryViewModel
import java.text.NumberFormat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class SummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummaryBinding
    private lateinit var viewModel: SummaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this, GenericViewModelFactory(this)).get(SummaryViewModel::class.java)

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        ThemeUtils.checkAndSetNightMode(this)

        viewModel.getTransactionsOfUser()
        viewModel.getPredictionsOfUser()
        setupObservers()
        setupBarChart()
    }

    private fun setupObservers() {
        viewModel.allPredictionsLiveData.observe(this) { predictions ->
            if (predictions.isNotEmpty()) {
                val mostRecentPrediction = predictions.maxByOrNull { it.year * 100 + it.month }

                mostRecentPrediction?.let {
                    val formattedPrediction = formatAsMoney(it.predicted_expense)
                    binding.TextExpensesPrediction.text = "\$$formattedPrediction"
                }
            } else {
                binding.TextExpensesPrediction.text =
                    "At least 2 months of transactions are required for predictions"
            }
        }

        viewModel.incomeLiveData.observe(this) { income ->
            val formattedIncome = formatAsMoney(income.toDouble())
            binding.TextIncomesTotal.text = "$ $formattedIncome COP"
        }

        viewModel.expenseLiveData.observe(this) { expense ->
            val formattedExpense = formatAsMoney(expense.toDouble())
            binding.TextExpensesTotal.text = "$ $formattedExpense COP"
        }

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

    private fun formatAsMoney(value: Double): String {
        val numberFormat = NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 0
        }
        return numberFormat.format(value)
    }

    private fun setupBarChart() {
        viewModel.allTransactionsLiveData.observe(this) { transactions ->
            val barChart = binding.barChart

            val expenseTransactions = transactions.filter { it.type == "Expense" }

            val categoryValues = expenseTransactions.groupBy { it.category }
                .mapValues { it.value.sumByDouble { transaction -> transaction.amount.toDouble() }.toFloat() }

            val entries = categoryValues.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), -entry.value)
            }

            val dataSet = BarDataSet(entries, "")
            dataSet.setDrawValues(true)
            dataSet.valueTextSize = 12f
            dataSet.colors = ColorTemplate.JOYFUL_COLORS.asList()

            val labels = categoryValues.keys.toList()

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

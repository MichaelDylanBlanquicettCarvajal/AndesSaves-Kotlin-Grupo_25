package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityHomeBinding
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.HomeViewModel
import java.text.NumberFormat

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        setupCardViewsNavigation()
        setupBalanceObserver()

        viewModel.getTransactionsOfUser()

        ThemeUtils.checkAndSetNightMode(this)
        if (ThemeUtils.isDarkModeEnabled(this)) {
            changeColorDarkMode()
        }
    }

    private fun setupBalanceObserver() {
        viewModel.balanceLiveData.observe(this) { balance ->
            val formattedBalance = formatBalance(balance)
            binding.textViewAmount.text = formattedBalance
            updateStatusBarAndBackground(balance)
        }
    }

    private fun formatBalance(balance: String): String {
        val numberFormat = NumberFormat.getNumberInstance()
        return try {
            val balanceValue = balance.toDouble()
            "$${numberFormat.format(balanceValue)} COP"
        } catch (e: NumberFormatException) {
            "$0.0 COP"
        }
    }

    private fun updateStatusBarAndBackground(balance: String) {
        val colorRes = if (balance.toDouble() >= 0) R.color.green else R.color.red
        window.statusBarColor = getColor(colorRes)
        binding.backGroundTop.setBackgroundColor(getColor(colorRes))
    }

    private fun setupCardViewsNavigation() {
        with(binding) {
            CardViewHistory.setOnClickListener { startActivity(Intent(this@HomeActivity, HistoryActivity::class.java)) }
            CardViewAddTransaction.setOnClickListener { startActivity(Intent(this@HomeActivity, AddTransactionActivity::class.java)) }
            CardViewSetting.setOnClickListener { startActivity(Intent(this@HomeActivity, SettingActivity::class.java)) }
            CardViewSummary.setOnClickListener { startActivity(Intent(this@HomeActivity, SummaryActivity::class.java)) }
            CardViewAccounts.setOnClickListener { startActivity(Intent(this@HomeActivity, AccountsActivity::class.java)) }
            CardViewTags.setOnClickListener { startActivity(Intent(this@HomeActivity, TagActivity::class.java)) }
            CardViewBudget.setOnClickListener { startActivity(Intent(this@HomeActivity, BudgetActivity::class.java)) }
        }
    }

    private fun changeColorDarkMode() {
        with(binding) {
            CardViewHistory.setCardBackgroundColor(getColor(R.color.black))
            CardViewBudget.setCardBackgroundColor(getColor(R.color.black))
            CardViewTags.setCardBackgroundColor(getColor(R.color.black))
            CardViewSummary.setCardBackgroundColor(getColor(R.color.black))
            CardViewAccounts.setCardBackgroundColor(getColor(R.color.black))
            CardViewSetting.setCardBackgroundColor(getColor(R.color.black))
            CardViewAddTransaction.setCardBackgroundColor(getColor(R.color.black))

            TextHistory.setTextColor(getColor(R.color.white))
            TextBudget.setTextColor(getColor(R.color.white))
            TextTags.setTextColor(getColor(R.color.white))
            TextSummary.setTextColor(getColor(R.color.white))
            TextAccounts.setTextColor(getColor(R.color.white))
            TextSettings.setTextColor(getColor(R.color.white))
            TextAddTransaction.setTextColor(getColor(R.color.light_gray))
        }
    }
}

package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityHomeBinding
import com.example.movilesapp.model.UserSingleton
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.HomeViewModel
import java.text.NumberFormat

class
HomeActivity : AppCompatActivity() {
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
        if(ThemeUtils.isDarkModeEnabled(this)){
            changeColorDarkMode()
        }

    }

    private fun setupBalanceObserver() {
        viewModel.balanceLiveData.observe(this) { balance ->
            val numberFormat = NumberFormat.getNumberInstance()
            try {
                val balanceValue = balance.toDouble()
                val formattedBalance = numberFormat.format(balanceValue)
                binding.textViewAmount.text = "$$formattedBalance COP"

                if (balanceValue >= 0){
                    window.statusBarColor = getColor(R.color.green)
                    binding.backGroundTop.setBackgroundColor(getColor(R.color.green))
                }
                else{
                    window.statusBarColor = getColor(R.color.red)
                    binding.backGroundTop.setBackgroundColor(getColor(R.color.red))
                }

            } catch (e: NumberFormatException) {
                binding.textViewAmount.text = "$0.0 COP"
            }
        }
    }

    private fun setupCardViewsNavigation() {
        binding.CardViewHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        binding.CardViewAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        binding.CardViewSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.CardViewSummary.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            startActivity(intent)
        }

        binding.CardViewAccounts.setOnClickListener {
            val intent = Intent(this, AccountsActivity::class.java)
            startActivity(intent)
        }

        binding.CardViewTags.setOnClickListener {
            val intent = Intent(this, TagActivity::class.java)
            startActivity(intent)
        }

        binding.CardViewBudget.setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun changeColorDarkMode(){
        binding.CardViewHistory.setCardBackgroundColor(getColor(R.color.black))
        binding.CardViewBudget.setCardBackgroundColor(getColor(R.color.black))
        binding.CardViewTags.setCardBackgroundColor(getColor(R.color.black))
        binding.CardViewSummary.setCardBackgroundColor(getColor(R.color.black))
        binding.CardViewAccounts.setCardBackgroundColor(getColor(R.color.black))
        binding.CardViewSetting.setCardBackgroundColor(getColor(R.color.black))
        binding.CardViewAddTransaction.setCardBackgroundColor(getColor(R.color.black))

        binding.TextHistory.setTextColor(getColor(R.color.white))
        binding.TextBudget.setTextColor(getColor(R.color.white))
        binding.TextTags.setTextColor(getColor(R.color.white))
        binding.TextSummary.setTextColor(getColor(R.color.white))
        binding.TextAccounts.setTextColor(getColor(R.color.white))
        binding.TextSettings.setTextColor(getColor(R.color.white))
        binding.TextAddTransaction.setTextColor(getColor(R.color.light_gray))
    }

}

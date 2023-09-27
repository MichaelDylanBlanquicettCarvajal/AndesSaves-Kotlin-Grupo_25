package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.databinding.ActivityHomeBinding
import com.example.movilesapp.model.UserSingleton
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
    }

    private fun setupBalanceObserver() {
        viewModel.balanceLiveData.observe(this) { balance ->
            val numberFormat = NumberFormat.getNumberInstance()
            try {
                val balanceValue = balance.toDouble()
                val formattedBalance = numberFormat.format(balanceValue)

                binding.textViewAmount.text = "$$formattedBalance COP"
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
            val intentSe = Intent(this, SettingActivity::class.java)
            startActivity(intentSe)
        }

        binding.CardViewAccount.setOnClickListener {
            val intentSu = Intent(this, AccountActivity::class.java)
            startActivity(intentSu)
        }
    }



}

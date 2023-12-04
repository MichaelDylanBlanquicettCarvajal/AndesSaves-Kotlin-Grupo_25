package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.movilesapp.databinding.ActivityAccountsBinding
import com.example.movilesapp.view.utilis.ThemeUtils

class AccountsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        ThemeUtils.checkAndSetNightMode(this)

        setUpBrowserNavigation()
    }

    private val openWebPage = { view: View, url: String ->
        view.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun setUpBrowserNavigation() {
        openWebPage(binding.Nequi, "https://www.nequi.com.co")
        openWebPage(binding.DaviPlata, "https://www.daviplata.com")
        openWebPage(binding.Paypal, "https://www.paypal.com")
    }
}

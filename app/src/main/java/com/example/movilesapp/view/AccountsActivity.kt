package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        ThemeUtils.checkAndSetNightMode(this)

        setUpBrowserNavigation()
    }

    private fun setUpBrowserNavigation(){
        // Navegar a la página web de Nequi
        binding.Nequi.setOnClickListener {
            val url = "https://www.nequi.com.co"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Navegar a la página web de DaviPlata
        binding.DaviPlata.setOnClickListener {
            val url = "https://www.daviplata.com"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Navegar a la página web de Paypal
        binding.Paypal.setOnClickListener {
            val url = "https://www.paypal.com"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

}
package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.movilesapp.databinding.ActivityAccountBinding
import com.example.movilesapp.view.utilis.ThemeUtils

class AccountActivity : AppCompatActivity() {
private lateinit var binding: ActivityAccountBinding
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.backButton.setOnClickListener {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        }

        ThemeUtils.checkAndSetNightMode(this)
        }
        }
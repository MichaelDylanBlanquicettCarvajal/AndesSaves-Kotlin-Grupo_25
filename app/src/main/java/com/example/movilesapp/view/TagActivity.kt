package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.movilesapp.databinding.ActivityTagBinding
import com.example.movilesapp.view.utilis.ThemeUtils

class TagActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTagBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setupBackButton()
        ThemeUtils.checkAndSetNightMode(this)
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}

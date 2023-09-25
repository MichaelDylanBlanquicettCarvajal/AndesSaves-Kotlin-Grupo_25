package com.example.movilesapp.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.movilesapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.CardViewHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        binding.CardViewSetting.setOnClickListener {
            val intentSe = Intent(this, SettingActivity::class.java)
            startActivity(intentSe)
        }
        binding.CardViewSummary.setOnClickListener {
            val intentSu = Intent( this, SummaryActivity::class.java)
            startActivity(intentSu)
        }
    }
}
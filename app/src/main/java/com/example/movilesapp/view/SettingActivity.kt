package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.databinding.ActivitySettingsBinding
import com.example.movilesapp.model.UserSingleton
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.SettingViewModel

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        ThemeUtils.checkAndSetNightMode(this)
        putNameAndEmailUser()
        observeNavigateToLoginActivity()
        setupLogOutButton()
    }

    private fun putNameAndEmailUser() {
        val userInfo = UserSingleton.getUserInfoSingleton()
        binding.UserName.text = userInfo?.name.orEmpty()
        binding.UserEmail.text = userInfo?.email.orEmpty()
    }

    private fun observeNavigateToLoginActivity() {
        viewModel.navigateToLoginActivity.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                navigateToLoginActivity()
                viewModel.navigateToLoginActivity.value = false
            }
        }
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setupLogOutButton() {
        binding.buttonSignOut.setOnClickListener {
            viewModel.signOut()
        }
    }
}

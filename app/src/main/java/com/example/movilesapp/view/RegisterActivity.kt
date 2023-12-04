package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityRegisterBinding
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        setupViews()
        observeErrorMessage()
        observeRegisterButton()
        setStatusBarColor()
        ThemeUtils.checkAndSetNightMode(this)
    }

    private fun setupViews() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val name = binding.editTextName.text.toString()
            val phone = binding.editTextPhone.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmationPassword = binding.editTextPasswordConfirm.text.toString()

            viewModel.registerWithEmailAndPassword(
                email,
                name,
                phone,
                password,
                confirmationPassword
            ) {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }

        binding.textViewLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun observeErrorMessage() {
        viewModel.errorMessageLiveData.observe(this) { errorMessage ->
            binding.textViewErrorMessage.text = if (errorMessage.isNotEmpty()) errorMessage else ""
        }
    }

    private fun observeRegisterButton() {
        viewModel.loading.observe(this) { isLoading ->
            binding.buttonLogin.isEnabled = !isLoading
            binding.buttonLogin.text = if (isLoading) "Loading..." else "Register"
        }
    }

    private fun setStatusBarColor() {
        val colorRes = if (ThemeUtils.isDarkModeEnabled(this)) R.color.black else R.color.white
        window.statusBarColor = getColor(colorRes)
    }
}

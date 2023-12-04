package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityLoginBinding
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        setupViews()
        observeErrorMessage()
        observeLoginButton()
        setStatusBarColor()
        ThemeUtils.checkAndSetNightMode(this)
    }

    private fun setupViews() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            viewModel.signInWithEmailAndPassword(email, password) {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }

        binding.textViewRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeErrorMessage() {
        viewModel.errorMessageLiveData.observe(this) { errorMessage ->
            binding.textViewErrorMessage.text = if (errorMessage.isNotEmpty()) errorMessage else ""
        }
    }

    private fun observeLoginButton() {
        viewModel.loading.observe(this) { isLoading ->
            binding.buttonLogin.isEnabled = !isLoading
            binding.buttonLogin.text = if (isLoading) "Loading..." else "LogIn"
        }
    }

    private fun setStatusBarColor() {
        val colorRes = if (ThemeUtils.isDarkModeEnabled(this)) R.color.black else R.color.white
        window.statusBarColor = getColor(colorRes)
    }
}

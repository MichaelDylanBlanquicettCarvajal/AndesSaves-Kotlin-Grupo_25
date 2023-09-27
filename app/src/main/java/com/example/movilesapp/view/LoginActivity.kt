package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityLoginBinding
import com.example.movilesapp.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = getColor(R.color.white)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        setupErrorMessageObserver()
        setupLoginButton()
        setupNavigationRegisterLink()
    }

    private fun setupErrorMessageObserver() {
        viewModel.errorMessageLiveData.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                binding.textViewErrorMessage.text = errorMessage
            } else {
                binding.textViewErrorMessage.text = ""
            }
        }
    }

    private fun setupLoginButton() {
        viewModel.loading.observe(this) { isLoading ->
            binding.buttonLogin.isEnabled = !isLoading
            binding.buttonLogin.text = if (isLoading) "Loading..." else "LogIn"
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            // Call ViewModel to Login
            viewModel.signInWithEmailAndPassword(email, password) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupNavigationRegisterLink() {
        binding.textViewRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}

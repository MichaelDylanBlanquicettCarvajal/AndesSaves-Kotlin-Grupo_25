package com.example.movilesapp.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.databinding.ActivityRegisterBinding
import com.example.movilesapp.viewmodel.LoginViewModel
import com.example.movilesapp.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        setupErrorMessageObserver()
        setupLoginButton()
        setupNavigationLoginLink()
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
            binding.buttonLogin.text = if (isLoading) "Loading..." else "Register"
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            // Call ViewModel to Login
            viewModel.registerWithEmailAndPassword(email, password) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupNavigationLoginLink() {
        binding.textViewLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}

package com.example.movilesapp.view

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityRegisterBinding
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.GenericViewModelFactory
import com.example.movilesapp.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel = ViewModelProvider(this, GenericViewModelFactory(this)).get(RegisterViewModel::class.java)

        setupErrorMessageObserver()
        setupRegisterButton()
        setupNavigationLoginLink()

        if (ThemeUtils.isDarkModeEnabled(this)) {
            window.statusBarColor = getColor(R.color.black)
        } else {
            window.statusBarColor = getColor(R.color.white)
        }
        ThemeUtils.checkAndSetNightMode(this)
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

    private fun setupRegisterButton() {
        viewModel.loading.observe(this) { isLoading ->
            binding.buttonLogin.isEnabled = !isLoading
            binding.buttonLogin.text = if (isLoading) "Loading..." else "Register"
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val name = binding.editTextName.text.toString()
            val phone = binding.editTextPhone.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmationPassword = binding.editTextPasswordConfirm.text.toString()

            if (isNetworkAvailable()) {
                // Call ViewModel to Register
                viewModel.registerWithEmailAndPassword(email, name, phone, password, confirmationPassword) {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }
            } else {
                binding.textViewErrorMessage.text = "There is no internet connection. Please check your connection"
            }
        }
    }

    private fun setupNavigationLoginLink() {
        binding.textViewLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

package com.example.movilesapp.view

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivitySettingsBinding
import com.example.movilesapp.model.UserSingleton
import com.example.movilesapp.view.utilis.ThemeUtils
import com.example.movilesapp.viewmodel.GenericViewModelFactory
import com.example.movilesapp.viewmodel.SettingViewModel

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel = ViewModelProvider(this, GenericViewModelFactory(this)).get(SettingViewModel::class.java)

        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        ThemeUtils.checkAndSetNightMode(this)

        putNameAndEmailUser()
        navigateToLoginActivity()
        setupLogOutButton()

        binding.CardViewAccounts.setOnClickListener {
            val intent = Intent(this, AccountsActivity::class.java)
            startActivity(intent)
        }

        binding.SuggestionsLayout.setOnClickListener {
            openSuggestionDialog()
        }

    }

    private fun openSuggestionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_suggestions, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        val buttonSendSuggestions = dialogView.findViewById<Button>(R.id.buttonSendSuggestions)

        buttonSendSuggestions.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun putNameAndEmailUser() {
        binding.UserName.text = UserSingleton.getUserInfoSingleton()?.name ?: ""
        binding.UserEmail.text = UserSingleton.getUserInfoSingleton()?.email ?: ""
    }

    private fun navigateToLoginActivity() {
        viewModel.navigateToLoginActivity.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                viewModel.navigateToLoginActivity.value = false
            }
        }
    }

    private fun setupLogOutButton(){
        binding.buttonSignOut.setOnClickListener{
            viewModel.signOut()
        }
    }


}
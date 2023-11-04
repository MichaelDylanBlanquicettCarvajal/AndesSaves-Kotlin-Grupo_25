package com.example.movilesapp.view

import AddTransactionViewModel
import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityAddTransactionBinding
import com.example.movilesapp.view.utilis.ThemeUtils
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.movilesapp.viewmodel.GenericViewModelFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: AddTransactionViewModel
    private var imageUri: String? = null

    private val CAMERA_PERMISSION_REQUEST = 100

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                val extras = data.extras
                if (extras != null) {
                    val imageBitmap = extras.get("data") as Bitmap?
                    if (imageBitmap != null) {
                        imageUri = bitmapToBase64(imageBitmap)
                        binding.imageView.setImageBitmap(imageBitmap)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this, GenericViewModelFactory(this)).get(AddTransactionViewModel::class.java)

        binding.LayoutExpenseCategory.visibility = View.GONE
        setupBackButton()
        setupErrorMessageObserver()
        setupToggleButtonTypeListeners()
        setupToggleButtonExCategoryListeners()
        setupAddTransactionButton()
        setupSelectImageButton()

        ThemeUtils.checkAndSetNightMode(this)

    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun setupSelectImageButton(){
        binding.btnSelectImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
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

    private fun setupToggleButtonTypeListeners() {
        binding.toggleButtonIncome.setOnClickListener {
            if (!binding.toggleButtonIncome.isChecked) {
                binding.toggleButtonIncome.isChecked = true
            }
            binding.toggleButtonExpense.isChecked = false
            binding.LayoutExpenseCategory.visibility = View.GONE

            binding.textViewTitle.text = "INCOME"
            binding.buttonAddTransaction.setBackgroundColor(getColor(R.color.green))
            binding.RelativeLayout.setBackgroundColor(getColor(R.color.green))
            window.statusBarColor = getColor(R.color.green)
            binding.btnSelectImage.setBackgroundColor(getColor(R.color.green))

            viewModel.loading.observe(this) { isLoading ->
                binding.buttonAddTransaction.isEnabled = !isLoading
                binding.buttonAddTransaction.text = if (isLoading) "Loading..." else "ADD INCOME"
            }
        }

        binding.toggleButtonExpense.setOnClickListener {
            if (!binding.toggleButtonExpense.isChecked) {
                binding.toggleButtonExpense.isChecked = true
            }
            binding.toggleButtonIncome.isChecked = false
            binding.LayoutExpenseCategory.visibility = View.VISIBLE

            binding.textViewTitle.text = "EXPENSE"
            binding.buttonAddTransaction.setBackgroundColor(getColor(R.color.red))
            binding.RelativeLayout.setBackgroundColor(getColor(R.color.red))
            window.statusBarColor = getColor(R.color.red)
            binding.btnSelectImage.setBackgroundColor(getColor(R.color.red))

            viewModel.loading.observe(this) { isLoading ->
                binding.buttonAddTransaction.isEnabled = !isLoading
                binding.buttonAddTransaction.text = if (isLoading) "Loading..." else "ADD EXPENSE"
            }
        }
    }

    private fun setupToggleButtonExCategoryListeners() {
        val toggleButtons = arrayOf(
            binding.toggleButtonFood,
            binding.toggleButtonTransport,
            binding.toggleButtonHouse,
            binding.toggleButtonOther
        )

        toggleButtons.forEach { button ->
            button.setOnClickListener {
                toggleButtons.forEach { otherButton ->
                    if (otherButton != button) {
                        otherButton.isChecked = false
                    }
                }
                if (!button.isChecked) {
                    button.isChecked = true
                }
            }
        }
    }

    private fun setupAddTransactionButton() {
        binding.buttonAddTransaction.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val amount = binding.editTextAmount.text.toString()

            val type = if (binding.toggleButtonIncome.isChecked) "Income" else "Expense"
            val category = if (type == "Income") "Income" else getCategoryValue()

            viewModel.createTransaction(name, amount, type, category, imageUri) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun getCategoryValue(): String {
        return when {
            binding.toggleButtonFood.isChecked -> "Food"
            binding.toggleButtonTransport.isChecked -> "Transport"
            binding.toggleButtonHouse.isChecked -> "House"
            else -> "Other"
        }
    }

}
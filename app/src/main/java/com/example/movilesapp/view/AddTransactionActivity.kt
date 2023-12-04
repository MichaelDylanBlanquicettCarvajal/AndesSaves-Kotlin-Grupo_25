package com.example.movilesapp.view

import AddTransactionViewModel
import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.movilesapp.R
import com.example.movilesapp.databinding.ActivityAddTransactionBinding
import com.example.movilesapp.view.utilis.ThemeUtils
import java.io.ByteArrayOutputStream

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: AddTransactionViewModel
    private var imageUri: String? = null

    private val CAMERA_PERMISSION_REQUEST = 100

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.extras?.get("data")?.let { imageBitmap ->
                imageUri = bitmapToBase64(imageBitmap as Bitmap)
                binding.imageView.setImageBitmap(imageBitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel = ViewModelProvider(this).get(AddTransactionViewModel::class.java)

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
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun setupSelectImageButton() {
        binding.btnSelectImage.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
    }

    private fun openCamera() {
        takePictureLauncher.launch(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupErrorMessageObserver() {
        viewModel.errorMessageLiveData.observe(this) { errorMessage ->
            binding.textViewErrorMessage.text = if (errorMessage.isNotEmpty()) errorMessage else ""
        }
    }

    private fun setupToggleButtonTypeListeners() {
        val greenColor = getColor(R.color.green)
        val redColor = getColor(R.color.red)

        binding.toggleButtonIncome.setOnClickListener { toggleButtonListener(greenColor, "INCOME") }
        binding.toggleButtonExpense.setOnClickListener { toggleButtonListener(redColor, "EXPENSE") }
    }

    private fun toggleButtonListener(color: Int, title: String) {
        val toggleButton = if (title == "INCOME") binding.toggleButtonIncome else binding.toggleButtonExpense
        val otherToggleButton = if (title == "INCOME") binding.toggleButtonExpense else binding.toggleButtonIncome

        if (!toggleButton.isChecked) {
            toggleButton.isChecked = true
        }
        otherToggleButton.isChecked = false
        binding.LayoutExpenseCategory.visibility = if (title == "EXPENSE") View.VISIBLE else View.GONE

        binding.textViewTitle.text = title
        binding.buttonAddTransaction.setBackgroundColor(color)
        binding.RelativeLayout.setBackgroundColor(color)
        window.statusBarColor = color
        binding.btnSelectImage.setBackgroundColor(color)

        viewModel.loading.observe(this) { isLoading ->
            binding.buttonAddTransaction.isEnabled = !isLoading
            binding.buttonAddTransaction.text = if (isLoading) "Loading..." else "ADD $title"
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
                startActivity(Intent(this, HomeActivity::class.java))
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

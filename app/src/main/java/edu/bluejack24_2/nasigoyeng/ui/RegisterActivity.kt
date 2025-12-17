package edu.bluejack24_2.nasigoyeng.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.data.supabase.SupabaseService
import edu.bluejack24_2.nasigoyeng.data.viewmodel.RegisterViewModel
import edu.bluejack24_2.nasigoyeng.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: RegisterViewModel
    private var selectedImageUri: Uri? = null
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var selectedCuisines: ArrayList<String>
    private lateinit var selectedAllergies: ArrayList<String>

    private val otpResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val file = uriToFile(selectedImageUri!!)
            viewModel.uploadImage(file)

            MainScope().launch {
                binding.registerBtn.isEnabled = false
                viewModel.profileImageUrl.collectLatest { url ->
                    if (url != null) {
                        viewModel.registerUser(
                            fullName = binding.fullNameTb.text.toString(),
                            mobileNumber = binding.mobilePhoneTb.text.toString(),
                            dob = binding.dobTb.text.toString(),
                            email = binding.emailTb.text.toString(),
                            password = binding.passwordTb.text.toString(),
                            height = binding.height.text.toString().toDouble(),
                            weight = binding.weight.text.toString().toDouble(),
                            imageUrl = url,
                            allergies = selectedAllergies,
                            preferences = selectedCuisines,
                            onSuccess = {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Registered successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                finish()
                            },
                            onError = {
                                Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_SHORT).show()
                                binding.registerBtn.isEnabled = true
                            }
                        )
                    }
                }
            }
        } else {
            Toast.makeText(this, "OTP verification cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            selectedImageUri = it.data?.data
            binding.profileImageView.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedCuisines = intent.getStringArrayListExtra("SELECTED_CUISINES") ?: arrayListOf()
        selectedAllergies = intent.getStringArrayListExtra("SELECTED_ALLERGIES") ?: arrayListOf()
        if (selectedCuisines.isEmpty()) {
            Log.w("RegisterActivity", "No cuisines received")
        } else {
            Log.d("RegisterActivity", "Received cuisines: $selectedCuisines")
        }

        if (selectedAllergies.isEmpty()) {
            Log.w("RegisterActivity", "No allergies received")
        } else {
            Log.d("RegisterActivity", "Received allergies: $selectedAllergies")
        }


        val supabase = SupabaseService.client

        viewModel = RegisterViewModel(
            supabase,
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )

        binding.backToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.registerBtn.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.checkInput(
                fullName = binding.fullNameTb.text.toString(),
                mobileNumber = binding.mobilePhoneTb.text.toString(),
                dob = binding.dobTb.text.toString(),
                height = binding.height.text.toString().toDouble(),
                weight = binding.weight.text.toString().toDouble(),
                email = binding.emailTb.text.toString(),
                password = binding.passwordTb.text.toString(),
                confirmPassword = binding.confirmPasswordTb.text.toString(),
                onError = {
                    Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_SHORT).show()
                    binding.registerBtn.isEnabled = true
                },
                onSuccess = {
                    val otpIntent = Intent(this@RegisterActivity, OtpRegisterActivity::class.java)
                    otpIntent.putExtra("EMAIL", binding.emailTb.text.toString())
                    otpResultLauncher.launch(otpIntent)
                }
            )
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("profile", ".jpg", cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}

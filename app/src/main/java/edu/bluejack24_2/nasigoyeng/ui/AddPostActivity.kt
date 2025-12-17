package edu.bluejack24_2.nasigoyeng.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.data.supabase.SupabaseService
import edu.bluejack24_2.nasigoyeng.data.viewmodel.AddPostViewModel
import edu.bluejack24_2.nasigoyeng.databinding.ActivityAddPostBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class AddPostActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null
    private lateinit var viewModel: AddPostViewModel
    private lateinit var binding: ActivityAddPostBinding

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            selectedImageUri = it.data?.data
            binding.postImage.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val supabase = SupabaseService.client

        viewModel = AddPostViewModel(
            supabase,
            FirebaseFirestore.getInstance()
        )

        val imageView = binding.postImage

        binding.backButton.setOnClickListener {
            finish()
        }

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.uploadButton.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val file = uriToFile(selectedImageUri!!)
            viewModel.uploadImage(file)

            MainScope().launch {
                binding.uploadButton.isEnabled = false
                viewModel.profileImageUrl.collectLatest { url ->
                    if (url != null) {
                        viewModel.uploadPost(
                            description = binding.captionInput.text.toString(),
                            imageUrl = url,
                            onSuccess = {
                                Toast.makeText(
                                    this@AddPostActivity,
                                    "Sukses menambahkan post",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            },
                            onError = {
                                Toast.makeText(this@AddPostActivity, it, Toast.LENGTH_SHORT).show()
                                binding.uploadButton.isEnabled = true
                            }
                        )
                    }
                }
            }
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
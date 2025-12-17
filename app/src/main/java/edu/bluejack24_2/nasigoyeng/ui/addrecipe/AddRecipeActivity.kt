package edu.bluejack24_2.nasigoyeng.ui.addrecipe

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.repository.RecipeRepository
import edu.bluejack24_2.nasigoyeng.data.viewmodel.AddRecipeViewModel
import edu.bluejack24_2.nasigoyeng.data.viewmodel.CuisineClassifierViewModel
import edu.bluejack24_2.nasigoyeng.databinding.ActivityAddRecipeBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.IngredientsAdapter
import edu.bluejack24_2.nasigoyeng.ui.adapter.InstructionAdapter
import kotlinx.coroutines.launch
import java.io.File
import android.view.View

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddRecipeBinding
    private lateinit var viewModel : AddRecipeViewModel
    private lateinit var cuisineClassifierViewModel : CuisineClassifierViewModel
    private lateinit var instructionAdapter: InstructionAdapter
    private lateinit var ingredientsAdapter: IngredientsAdapter
    private var selectedImageUri: Uri? = null
    private var isAdmin: Boolean = false

    private lateinit var res : String

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            selectedImageUri = it.data?.data
            binding.ivRecipeImage.setImageURI(selectedImageUri)
            Log.d("AddRecipeActivity", "Image selected: ${selectedImageUri?.path}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("AddRecipe", "onCreate called")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isAdmin = intent.getBooleanExtra("is_admin", false)

        binding.officialBadge.visibility = if (isAdmin) View.VISIBLE else View.GONE

        initViewModel()
        setupRecyclerViews()
        setupClickListeners()
        setupObservers()

    }

    private fun initViewModel(){
        Log.d("AddRecipeActivity", "init view model")

        viewModel = ViewModelProvider(this)[AddRecipeViewModel::class.java]
    }


    private fun setupRecyclerViews(){
        instructionAdapter = InstructionAdapter(
            onTextChanged = { position, text ->  viewModel.updateStepText(position, text)},
            onDeleteClick = { position -> viewModel.removeStep(position)}
        )

        ingredientsAdapter = IngredientsAdapter(
            onTextChanged = {position, text -> viewModel.updateIngredientText(position, text) },
            onDeleteClick = {position -> viewModel.removeIngridient(position)}
        )

        binding.rvInstructions.apply {
            adapter = instructionAdapter
            layoutManager = LinearLayoutManager(this@AddRecipeActivity)
            isNestedScrollingEnabled = false
        }

        binding.rvIngredients.apply {
            adapter = ingredientsAdapter
            layoutManager = LinearLayoutManager(this@AddRecipeActivity)
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
        }
    }

    private fun setupClickListeners(){
        binding.btnAddInstruction.setOnClickListener {
            viewModel.addStep()
        }

        binding.btnAddIngredient.setOnClickListener{
            viewModel.addIngredient()
        }

        binding.imageContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)

        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPublish.setOnClickListener {
            Log.d("AddRecipeActivity", "publish recipe")

            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select a recipe image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val file = uriToFile(selectedImageUri!!)

            lifecycleScope.launch {
                binding.btnPublish.isEnabled = false

                try {
                    val imageUrl = viewModel.uploadImage(file)

                    viewModel.setImageUrl(imageUrl)
                    viewModel.setTitle(binding.etRecipeName.text.toString())
                    viewModel.setTime(binding.etCookingTime.text.toString())
                    viewModel.setDescription(binding.etRecipeDescription.text.toString())
                    viewModel.setCategory(binding.spinnerCategory.selectedItem.toString())
                    viewModel.setCalories(binding.etCalories.text.toString())
                    viewModel.saveRecipe(isAdmin)

                } catch (e: Exception) {
                    Toast.makeText(this@AddRecipeActivity, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.btnPublish.isEnabled = true
                }
            }
        }

    }

    private fun setupObservers(){

        // observer steps
        viewModel.steps.observe(this) { steps ->
            Log.d("Activity", "steps observed. Size: ${steps.size}")
            instructionAdapter.updateInstructions(steps)
        }

        viewModel.ingredients.observe(this){ingredients ->
            Log.d("Activity", "ingredients observed. Size: ${ingredients.size}")
            ingredientsAdapter.updateIngredient(ingredients)
        }

        viewModel.categoryList.observe(this){categories ->
            val adapter = ArrayAdapter(this, R.layout.item_spinner_dropdown, categories)
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            binding.spinnerCategory.adapter = adapter
        }

        viewModel.saveResult.observe(this) { result ->
            result?.onSuccess { status ->
                val message = if (status == "Update") {
                    "Recipe updated successfully!"
                } else {
                    "Recipe saved successfully!"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//                finish()
            }

            result?.onFailure { error ->
                Toast.makeText(this, "Failed to save recipe: ${error.message}", Toast.LENGTH_LONG).show()
            }

//            Log.d("Activity", "Prediction Result $res")

//            Toast.makeText(this, res, Toast.LENGTH_SHORT).show()

        }

        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // observer error message
//        viewModel.showMinimumError.observe(this) {errorMes}

        viewModel.isLoading.observe(this){isLoading ->
            binding.btnPublish.isEnabled = !isLoading
            binding.btnAddInstruction.isEnabled = !isLoading
            binding.btnAddIngredient.isEnabled = !isLoading
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
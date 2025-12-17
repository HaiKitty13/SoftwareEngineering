package edu.bluejack24_2.nasigoyeng.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack24_2.nasigoyeng.data.models.Category
import edu.bluejack24_2.nasigoyeng.data.models.Ingredient
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.data.models.Step
import edu.bluejack24_2.nasigoyeng.data.repository.RecipeRepository
import edu.bluejack24_2.nasigoyeng.data.supabase.SupabaseService
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AddRecipeViewModel (
    private val repository: RecipeRepository = RecipeRepository()
): ViewModel(){

    val supabase = SupabaseService.client

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _country = MutableLiveData<String>()
    val country: LiveData<String> = _country

    private val _time = MutableLiveData<String>()
    val time: LiveData<String> = _time

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

    // Ingredients
    private val _ingredients = MutableLiveData<MutableList<Ingredient>>()
    val ingredients: LiveData<MutableList<Ingredient>> = _ingredients

    // Steps
    private val _steps = MutableLiveData<MutableList<Step>>()
    val steps: LiveData<MutableList<Step>> = _steps

    private val _categories = MutableLiveData<MutableList<Category>>()
    val categories: LiveData<MutableList<Category>> = _categories

    private val _calories = MutableLiveData<String>()
    val calories: LiveData<String> = _calories

    // UI States
    private val _showMinimumError = MutableLiveData<String>()
    val showMinimumError: LiveData<String> = _showMinimumError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveResult = MutableLiveData<Result<String>>()
    val saveResult: LiveData<Result<String>> = _saveResult

    private var currentRecipeId: String? = null

    val categoryList = MutableLiveData<List<String>>()

    private val category = MutableLiveData<String>()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage


    init {
        initializeData()
    }

    private fun initializeData(){
        _ingredients.value = mutableListOf(Ingredient(""))
        _steps.value = mutableListOf(Step(""))
        _categories.value = mutableListOf(Category(""))
        _title.value = ""
        _description.value = ""
        _calories.value = ""
        _country.value = ""
        _time.value = ""
        _imageUrl.value = ""
        categoryList.value = listOf("Breakfast", "Lunch", "Snack", "Dinner")
        category.value = ""
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun setCountry(country: String) {
        _country.value = country
    }

    fun setTime(time: String) {
        _time.value = time
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setCalories(calories: String) {
        _calories.value = calories
    }

    fun setImageUrl(imageUrl: String) {
        _imageUrl.value = imageUrl
    }

    fun setCategory(cat: String){
        category.value = cat
    }

    fun addIngredient(){
        val currentList = _ingredients.value?.toMutableList()?: mutableListOf()
        currentList.add(Ingredient(""))
        _ingredients.value = currentList
        Log.d("ViewModel", "addIngredient called. Size: ${currentList.size}")
    }

    fun removeIngridient(position: Int){
        val currentList = _ingredients.value?.toMutableList()?: mutableListOf()
        if(currentList.size > 1){
            currentList.removeAt(position)
            _ingredients.value = currentList
        }
    }

    fun updateIngredientText(position: Int, text: String){
        val currentList = _ingredients.value?.toMutableList() ?: mutableListOf()
        if (position < currentList.size){
            currentList[position] = currentList[position].copy(text = text)
            _ingredients.value = currentList
        }
    }

    fun addStep() {
        val currentList = _steps.value?.toMutableList() ?: mutableListOf()
        currentList.add(Step(""))
        _steps.value = currentList
    }

    fun removeStep(position: Int) {
        val currentList = _steps.value?.toMutableList() ?: mutableListOf()
        if (currentList.size > 1) {
            currentList.removeAt(position)
            _steps.value = currentList
        } else {
            _showMinimumError.value = "At least one step is required"
        }
    }

    fun updateStepText(position: Int, text: String) {
        val currentList = _steps.value?.toMutableList() ?: mutableListOf()
        if (position < currentList.size) {
            currentList[position] = currentList[position].copy(text = text)
            _steps.value = currentList
        }
    }

    suspend fun uploadImage(file: File): String {
        return try {
            val fileName = "recipe_image/${UUID.randomUUID()}.jpg"
            supabase.storage.from("nasigoyengenak").upload(
                path = fileName,
                file = file,
                upsert = true
            )
            val imageUrl = supabase.storage.from("nasigoyengenak").publicUrl(fileName)
            Log.d("SupabaseUpload", "Image uploaded successfully: $imageUrl")
            imageUrl
        } catch (e: Exception) {
            Log.e("SupabaseUpload", "Error uploading image: ${e.message}", e)
            throw e
        }
    }


    fun addCategory() {
        val currentList = _categories.value?.toMutableList() ?: mutableListOf()
        currentList.add(Category(""))
        _categories.value = currentList
    }

    fun removeCategory(position: Int) {
        val currentList = _categories.value?.toMutableList() ?: mutableListOf()
        if (currentList.size > 1) {
            currentList.removeAt(position)
            _categories.value = currentList
        } else {
            _showMinimumError.value = "At least one category is required"
        }
    }

//    fun updateCategoryText(position: Int, text: String) {
//        val currentList = _categories.value?.toMutableList() ?: mutableListOf()
//        if (position < currentList.size) {
//            currentList[position] = currentList[position].copy(text = text)
//            _categories.value = currentList
//        }
//    }

    fun validateRecipe(): Boolean{
        Log.d("ViewModel", "=== Starting validateRecipe ===")

        // Debug title
        Log.d("ViewModel", "Title: '${_title.value}' - isNullOrBlank: ${_title.value.isNullOrBlank()}")
        if (_title.value.isNullOrBlank()){
            _showMinimumError.value = "Title is required"
            Log.d("ViewModel", "Validation failed: Title is required")
            return false
        }

        if (_calories.value.isNullOrBlank()){
            _showMinimumError.value = "Calories is required"
            Log.d("ViewModel", "Validation failed: Calories is required")
            return false
        }

        Log.d("ViewModel", "Description: '${_description.value}' - isNullOrBlank: ${_description.value.isNullOrBlank()}")
        if (_description.value.isNullOrBlank()) {
            _showMinimumError.value = "Description is required"
            Log.d("ViewModel", "Validation failed: Description is required")
            return false
        }

//        if (_imageUrl.value.isNullOrBlank()) {
//            _showMinimumError.value = "Image is required"
//            Log.d("ViewModel", "Validation failed: Image is required")
//            return false
//        }

        Log.d("ViewModel", "Ingredients count: ${_ingredients.value?.size}")
        _ingredients.value?.forEachIndexed { index, ingredient ->
            Log.d("ViewModel", "Ingredient $index: '${ingredient.text}' - trimmed: '${ingredient.text.trim()}' - isEmpty: ${ingredient.text.trim().isEmpty()}")
        }

        val validIngredients = _ingredients.value?.filter { it.text.trim().isNotEmpty() }
        Log.d("ViewModel", "Valid ingredients count: ${validIngredients?.size}")
        if (validIngredients.isNullOrEmpty()) {
            _showMinimumError.value = "At least one ingredient is required"
            Log.d("ViewModel", "Validation failed: At least one ingredient is required")
            return false
        }

        Log.d("ViewModel", "Steps count: ${_steps.value?.size}")
        _steps.value?.forEachIndexed { index, step ->
            Log.d("ViewModel", "Step $index: '${step.text}' - trimmed: '${step.text.trim()}' - isEmpty: ${step.text.trim().isEmpty()}")
        }

        val validSteps = _steps.value?.filter { it.text.trim().isNotEmpty() }
        Log.d("ViewModel", "Valid steps count: ${validSteps?.size}")
        if (validSteps.isNullOrEmpty()) {
            _showMinimumError.value = "At least one step is required"
            Log.d("ViewModel", "Validation failed: At least one step is required")
            return false
        }

        Log.d("ViewModel", "Categories count: ${_categories.value?.size}")
        _categories.value?.forEachIndexed { index, category ->
            Log.d("ViewModel", "Category $index: '${category.name}' - trimmed: '${category.name.trim()}' - isEmpty: ${category.name.trim().isEmpty()}")
        }

        val currentCategories = _categories.value?.toMutableList() ?: mutableListOf()
        Log.d("ViewModel", "Current categories size: ${currentCategories.size}")
        Log.d("ViewModel", "All categories empty: ${currentCategories.all { it.name.trim().isEmpty() }}")

        if (currentCategories.isEmpty() || currentCategories.all { it.name.trim().isEmpty() }) {
            Log.d("ViewModel", "Setting default category")
            _categories.value = mutableListOf(Category("0"))
        } else {
            val cleaned = currentCategories.filter { it.name.trim().isNotEmpty() }
            Log.d("ViewModel", "Cleaned categories count: ${cleaned.size}")
            _categories.value = cleaned.toMutableList()
        }

        Log.d("ViewModel", "=== Validation passed ===")
        return true
    }

    fun saveRecipe(isOfficial : Boolean = false) {
        Log.d("ViewModel", "=== saveRecipe called ===")

        val validationResult = validateRecipe()
        Log.d("ViewModel", "Validation result: $validationResult")

        if (!validationResult) {
            val errorMessage = _showMinimumError.value ?: "Please check your input"
            _toastMessage.value = errorMessage
            Log.d("ViewModel", "Validation failed, returning early")
            return
        }

        Log.d("ViewModel", "save recipe - validation passed")

        _isLoading.value = true

        val recipe = Recipe(
            title = _title.value ?: "",
            description = _description.value ?: "",
            country = _country.value ?: "",
            time = _time.value?.toIntOrNull() ?: 0,
            image_url = _imageUrl.value ?: "",
            ingredients = _ingredients.value?.mapNotNull {
                if (it.text.trim().isNotEmpty()) it.text.trim() else null
            } ?: emptyList(),
            steps = _steps.value?.mapNotNull {
                if (it.text.trim().isNotEmpty()) it.text.trim() else null
            } ?: emptyList(),
            category = category.value?: "",
            calories = _calories.value?.toIntOrNull() ?: 0,
            created_by = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            is_official = isOfficial,
            liked_by = emptyList(),
            bookmark_by = emptyList(),
        )

        Log.d("AddRecipeViewModel", "Recipe created: $recipe")

        viewModelScope.launch{
            try {
                Log.d("ViewModel", "Starting repository call")
                val result = if (currentRecipeId!= null){
                    Log.d("ViewModel", "Updating recipe with ID: $currentRecipeId")
                    repository.updateRecipe(currentRecipeId!!, recipe)
                    Result.success("Update")
                } else {
                    Log.d("ViewModel", "Saving new recipe")
                    repository.saveRecipe(recipe)
                }
                Log.d("ViewModel", "Repository call completed: $result")
                _saveResult.value = result
            } catch (e: Exception){
                Log.e("ViewModel", "Exception during save: ${e.message}", e)
                _saveResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
                Log.d("ViewModel", "=== saveRecipe completed ===")
            }
        }
    }

}
package edu.bluejack24_2.nasigoyeng.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.bumptech.glide.Glide
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.models.Category
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.databinding.FragmentHomeBinding
import edu.bluejack24_2.nasigoyeng.ui.RecipeDetailActivity
import edu.bluejack24_2.nasigoyeng.ui.adapter.CategoryAdapter
import edu.bluejack24_2.nasigoyeng.ui.adapter.HistoryAdapter
import edu.bluejack24_2.nasigoyeng.ui.adapter.OnRecipeActionListener
import edu.bluejack24_2.nasigoyeng.ui.adapter.RecommendationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import edu.bluejack24_2.nasigoyeng.databinding.ItemRecomendationBinding
import org.apache.commons.text.similarity.LevenshteinDistance

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val categoryList = mutableListOf<Category>()
    private lateinit var categoryAdapter: CategoryAdapter

    private val recommendedList = mutableListOf<Recipe>()
    private lateinit var recommendationAdapter: RecommendationAdapter

    private val historyList = mutableListOf<Recipe>()
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var currentUserId: String

    private val database = FirebaseDatabase.getInstance("https://nasigoyeng-5c083-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserId = auth.currentUser?.uid ?: ""

        setupCategoryRecycler()
        setupRecommendationRecycler()
        setupHistoryRecycler()
        fetchCategories()
        getUserRecommendation()
        fetchUserHistory()

        getMealPlanForUser();
    }

    private fun setupCategoryRecycler() {
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            openCategoryDetailFragment(category)
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvCategories)
    }

    private fun setupRecommendationRecycler() {
        recommendationAdapter = RecommendationAdapter(recommendedList, currentUserId, object :
            OnRecipeActionListener {
            override fun onLikeClicked(recipe: Recipe) {
                toggleLike(recipe)
            }

            override fun onBookmarkClicked(recipe: Recipe) {
                toggleBookmark(recipe)
            }

            override fun onRecipeClicked(recipe: Recipe){
                redirectToRecipeDetail(recipe)
            }
        })
        binding.rvRecommendations.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommendations.adapter = recommendationAdapter
    }

    private fun setupHistoryRecycler() {
        historyAdapter = HistoryAdapter(historyList, object :
            OnRecipeActionListener {
            override fun onLikeClicked(recipe: Recipe) {
                toggleLike(recipe)
            }

            override fun onBookmarkClicked(recipe: Recipe) {
                toggleBookmark(recipe)
            }

            override fun onRecipeClicked(recipe: Recipe){
                redirectToRecipeDetail(recipe)
            }
        })
        binding.rvRecentlyViewed.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentlyViewed.adapter = historyAdapter
    }

    private fun fetchCategories() {
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { documents ->
                categoryList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val imageUrl = document.getString("image_url") ?: ""

                    val category = Category(
                        id = document.id,
                        name = name,
                        imageUrl = imageUrl
                    )

                    categoryList.add(category)
                }
                categoryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error fetching categories", exception)
            }
    }

    private fun getUserRecommendation() {

        if (currentUserId != null) {
            firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val preferences = document.get("preferences") as? List<*> ?: emptyList<Any>()
                        val allergies = document.get("allergies") as? List<*> ?: emptyList<Any>()

                        val prefList = preferences.filterIsInstance<String>()
                        val allergyList = allergies.filterIsInstance<String>()

                        Log.d("HomeFragment", "Preferences: $prefList")
                        Log.d("HomeFragment", "Allergies: $allergyList")

                        fetchRecommendation(prefList, allergyList)
                    } else {
                        Log.d("HomeFragment", "User document not found")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Error getting user preferences and allergies", exception)
                }
        } else {
            Log.e("HomeFragment", "User not logged in")
        }
    }

    private fun fetchRecommendation(prefList: List<String>, allergyList: List<String>) {
        firestore.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                val recommendedRecipes = mutableListOf<Recipe>()

                for (document in result) {
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    val category = document.getString("category") ?: ""
                    val ingredients = document.get("ingredients") as? List<String> ?: emptyList()

                    val normalizedIngredients = ingredients.map { normalize(it) }
                    val normalizedContent = listOf(name, description, category).map { normalize(it) } + normalizedIngredients
                    val normalizedPrefs = prefList.map { normalize(it) }
                    val normalizedAllergies = allergyList.map { normalize(it) }

                    val isMatchingPreference = normalizedPrefs.any { pref ->
                        normalizedContent.any { field ->
                            field.contains(pref)
                        }
                    }

                    val containsAllergy = normalizedAllergies.any { allergy ->
                        normalizedIngredients.any { ingredient ->
                            isSimilar(ingredient, allergy)
                        }
                    }

                    if (isMatchingPreference && !containsAllergy) {
                        val title = document.getString("title") ?: ""
                        val description = document.getString("description") ?: ""
                        val image_url = document.getString("image_url") ?: ""
                        val time = document.getLong("time")?.toInt() ?: 0
                        val category = document.getString("category") ?: ""
                        val calories = document.getLong("calories")?.toInt() ?: 0
                        val isOfficial = document.getBoolean("_official") ?: false
                        val liked_by = document.get("liked_by") as? List<String> ?: emptyList()
                        val bookmark_by = document.get("bookmark_by") as? List<String> ?: emptyList()

                        val recipe = Recipe(
                            id = document.id,
                            time = time,
                            image_url = image_url,
                            created_by = "",
                            steps = arrayListOf(),
                            title = title,
                            description = description,
                            ingredients = arrayListOf(),
                            country = "",
                            calories = calories,
                            category = category,
                            is_official = isOfficial,
                            liked_by = liked_by,
                            bookmark_by = bookmark_by
                        )
                        recommendedRecipes.add(recipe)
//                        recommendedList.add(recipe)
                    }
                }

                Log.d("HomeFragment", "Recommended Recipes: ${recommendedRecipes.size}")

                recommendationAdapter.updateList(recommendedRecipes)
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching recipes", e)
            }
    }

    private fun fetchUserHistory() {
        val historyRef = database.child("history").child(currentUserId)

        historyRef.orderByValue().limitToLast(10).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipeIds = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val id = child.key
                        Log.d("HomeFragment", "History Recipe ID: $id")
                        if (!id.isNullOrEmpty()) recipeIds.add(id)
                    }

                    recipeIds.reverse()
                } else {
                    Log.d("HomeFragment", "No snapshot data exists.")
                }

                Log.d("HomeFragment", "Fetched ${recipeIds.size} history recipe IDs")

                if (recipeIds.isNotEmpty()) {
                    fetchRecipesByIds(recipeIds)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Error reading history", error.toException())
            }
        })
    }


    private fun fetchRecipesByIds(ids: List<String>) {
        firestore.collection("recipes")
            .whereIn(FieldPath.documentId(), ids)
            .get()
            .addOnSuccessListener { result ->
                val historyRecipes = mutableListOf<Recipe>()

                for(document in result) {
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val image_url = document.getString("image_url") ?: ""
                    val time = document.getLong("time")?.toInt() ?: 0
                    val category = document.getString("category") ?: ""
                    val calories = document.getLong("calories")?.toInt() ?: 0
                    val isOfficial = document.getBoolean("_official") ?: false
                    val liked_by = document.get("liked_by") as? List<String> ?: emptyList()
                    val bookmark_by = document.get("bookmark_by") as? List<String> ?: emptyList()

                    val recipe = Recipe(
                        id = document.id,
                        time = time,
                        image_url = image_url,
                        created_by = "",
                        steps = arrayListOf(),
                        title = title,
                        description = description,
                        ingredients = arrayListOf(),
                        country = "",
                        calories = calories,
                        category = category,
                        is_official = isOfficial,
                        liked_by = liked_by,
                        bookmark_by = bookmark_by
                    )

                    historyRecipes.add(recipe)
                }

                historyAdapter.updateList(historyRecipes)
                Log.d("HomeFragment", "Fetched ${historyRecipes.size} history recipes")
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching recipes by history", e)
            }
    }

    private fun calculateDailyCalories(weightKg: Double, heightCm: Double): Int {
        val heightM = heightCm / 100
        val bmi = weightKg / (heightM * heightM)

        val (minFactor, maxFactor) = when {
            bmi < 18.5 -> 35.0 to 40.0       // Underweight
            bmi < 25 -> 30.0 to 32.0        // Normal
            bmi < 30 -> 25.0 to 28.0        // Overweight
            else -> 20.0 to 25.0            // Obese
        }

        return (((minFactor + maxFactor) / 2) * weightKg).toInt()
    }

    private fun getMealPlanForUser() {
        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                val weight = doc.getDouble("weight") ?: 0.0
                val height = doc.getDouble("height") ?: 0.0

                if (weight == 0.0 || height == 0.0) {
                    Log.e("MealPlan", "User has no weight/height data")
                    return@addOnSuccessListener
                }

                val dailyCalories = calculateDailyCalories(weight, height)

                val breakfastCal = (dailyCalories * 0.30).toInt()
                val lunchCal = (dailyCalories * 0.40).toInt()
                val dinnerCal = (dailyCalories * 0.30).toInt()

                getRandomRecipeFor("breakfast", breakfastCal)
                getRandomRecipeFor("lunch", lunchCal)
                getRandomRecipeFor("dinner", dinnerCal)
            }
    }

    private fun getRandomRecipeFor(mealType: String, maxCalories: Int) {
        firestore.collection("recipes")
            .whereLessThanOrEqualTo("calories", maxCalories)
            .get()
            .addOnSuccessListener { result ->

                // --- Ambil alergi user lebih dahulu ---
                firestore.collection("users")
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener { userDoc ->

                        val allergies = (userDoc.get("allergies") as? List<String>) ?: emptyList()

                        val normalizedAllergies = allergies.map { it.trim().lowercase() }

                        val filteredRecipes = result.mapNotNull { doc ->

                            val ingredients = doc.get("ingredients") as? List<String> ?: emptyList()

                            val normalizedIngredients = ingredients.map { it.trim().lowercase() }

                            val containsAllergy = normalizedAllergies.any { allergy ->
                                normalizedIngredients.any { ingredient ->
                                    isSimilar(ingredient, allergy)
                                }
                            }

                            if (containsAllergy) {
                                null
                            } else {
                                Recipe(
                                    id = doc.id,
                                    time = doc.getLong("time")?.toInt() ?: 0,
                                    image_url = doc.getString("image_url") ?: "",
                                    created_by = "",
                                    steps = arrayListOf(),
                                    title = doc.getString("title") ?: "",
                                    description = doc.getString("description") ?: "",
                                    ingredients = ArrayList(ingredients),
                                    country = "",
                                    calories = doc.getLong("calories")?.toInt() ?: 0,
                                    category = doc.getString("category") ?: "",
                                    is_official = doc.getBoolean("_official") ?: false,
                                    liked_by = doc.get("liked_by") as? List<String> ?: emptyList(),
                                    bookmark_by = doc.get("bookmark_by") as? List<String> ?: emptyList()
                                )
                            }
                        }

                        if (filteredRecipes.isNotEmpty()) {
                            val randomRecipe = filteredRecipes.random()
                            updateMealUI(mealType, randomRecipe)
                        } else {
                            Log.w("MealPlan", "No safe recipe found for $mealType")
                        }
                    }
            }
    }

    private fun addMealItem(container: LinearLayout, recipe: Recipe) {
        val binding = ItemRecomendationBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )

        // Title & Description
        binding.textTitle.text = recipe.title
        binding.textDescription.text = recipe.description
        binding.textTime.text = "${recipe.time} min"

        // Official badge
        val isAdmin = recipe.is_official ?: false
        binding.ivOfficialIcon.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // Image
        Glide.with(requireContext())
            .load(recipe.image_url)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.imageFood)

        // Bookmark & Like icon (optional)
        val isLiked = recipe.liked_by.contains(currentUserId)
        binding.imageFavorite.setBackgroundResource(
            if (isLiked) R.drawable.circle_pink_background
            else R.drawable.circle_unclick_background
        )

        val isBookmarked = recipe.bookmark_by.contains(currentUserId)
        binding.imageBookmark.setBackgroundResource(
            if (isBookmarked) R.drawable.circle_pink_background
            else R.drawable.circle_unclick_background
        )

        // Optional clicks (redirect detail, like, bookmark)
        binding.root.setOnClickListener {
            redirectToRecipeDetail(recipe)
        }

        container.addView(binding.root)
    }

    private fun updateMealUI(mealType: String, recipe: Recipe) {
        val caloriesText = "${recipe.calories} kcal"

        when (mealType) {
            "breakfast" -> {
                binding.containerBreakfast.removeAllViews()
                addMealItem(binding.containerBreakfast, recipe)
            }
            "lunch" -> {
                binding.containerLunch.removeAllViews()
                addMealItem(binding.containerLunch, recipe)
            }
            "dinner" -> {
                binding.containerDinner.removeAllViews()
                addMealItem(binding.containerDinner, recipe)
            }
        }
    }

    fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun isSimilar(a: String, b: String, maxDistance: Int = 2): Boolean {
        val distance = LevenshteinDistance.getDefaultInstance().apply(a.lowercase(), b.lowercase())
        return distance != -1 && distance <= maxDistance
    }

    private fun openCategoryDetailFragment(category: Category) {
        val bundle = Bundle().apply {
            putString("categoryId", category.id)
            putString("categoryName", category.name)
        }

        val categoryDetailFragment = CategoryDetailFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, categoryDetailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun toggleLike(recipe: Recipe) {
        val recipeRef = firestore.collection("recipes").document(recipe.id)
        val userRef = firestore.collection("users").document(currentUserId)

        val isLiked = recipe.liked_by.contains(currentUserId)

        val recipeUpdate = if (isLiked) {
            recipe.liked_by = recipe.liked_by.filter { it != currentUserId }
            mapOf("liked_by" to FieldValue.arrayRemove(currentUserId))
        } else {
            recipe.liked_by = recipe.liked_by + currentUserId
            mapOf("liked_by" to FieldValue.arrayUnion(currentUserId))
        }

        val userUpdate = if (isLiked) {
            mapOf("like_recipe" to FieldValue.arrayRemove(recipe.id))
        } else {
            mapOf("like_recipe" to FieldValue.arrayUnion(recipe.id))
        }

        recipeRef.update(recipeUpdate)
            .addOnSuccessListener {
                userRef.update(userUpdate)
                    .addOnSuccessListener {
                        recommendationAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                    }
            }
    }

    private fun toggleBookmark(recipe: Recipe) {
        val firestore = FirebaseFirestore.getInstance()
        val recipeRef = firestore.collection("recipes").document(recipe.id)
        val userRef = firestore.collection("users").document(currentUserId)

        val isBookmarked = recipe.bookmark_by.contains(currentUserId)

        val recipeUpdate = if (isBookmarked) {
            recipe.bookmark_by = recipe.bookmark_by.filter { it != currentUserId }
            mapOf("bookmark_by" to FieldValue.arrayRemove(currentUserId))
        } else {
            recipe.bookmark_by = recipe.bookmark_by + currentUserId
            mapOf("bookmark_by" to FieldValue.arrayUnion(currentUserId))
        }

        val userUpdate = if (isBookmarked) {
            mapOf("bookmark_recipes" to FieldValue.arrayRemove(recipe.id))
        } else {
            mapOf("bookmark_recipes" to FieldValue.arrayUnion(recipe.id))
        }

        recipeRef.update(recipeUpdate)
            .addOnSuccessListener {
                userRef.update(userUpdate)
                    .addOnSuccessListener {
                        recommendationAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                    }
            }
    }

    private fun redirectToRecipeDetail(recipe: Recipe) {
        val intent = Intent(context, RecipeDetailActivity::class.java).apply {
            putExtra("RECIPE_ID", recipe.id)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package edu.bluejack24_2.nasigoyeng.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.databinding.FragmentCategoryDetailBinding
import edu.bluejack24_2.nasigoyeng.ui.RecipeDetailActivity
import edu.bluejack24_2.nasigoyeng.ui.adapter.CategoryDetailAdapter
import edu.bluejack24_2.nasigoyeng.ui.adapter.OnRecipeActionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CategoryDetailFragment : Fragment() {

    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryId: String
    private lateinit var categoryName: String

    private val recipes = mutableListOf<Recipe>()
    private val allRecipes = mutableListOf<Recipe>()
    private lateinit var adapter: CategoryDetailAdapter

    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getString("categoryId") ?: ""
            categoryName = it.getString("categoryName") ?: ""
        }
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        setupRecyclerView()

        when (categoryName) {
            "Breakfast" -> binding.chipBreakfast.isChecked = true
            "Lunch" -> binding.chipLunch.isChecked = true
            "Dinner" -> binding.chipDinner.isChecked = true
            "Snack" -> binding.chipSnack.isChecked = true
//            "Dessert" -> binding.chipDessert.isChecked = true
        }

        setupChipListeners()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                filterRecipes(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        fetchRecipes()
    }


    private fun setupRecyclerView() {
        adapter = CategoryDetailAdapter(recipes, currentUserId, object : OnRecipeActionListener {
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

        binding.rvFoodItems.adapter = adapter
        binding.rvFoodItems.setHasFixedSize(true)
    }

    private fun fetchRecipes() {
        firestore.collection("recipes")
            .get()
            .addOnSuccessListener { documents ->
                if (!isAdded || context == null) return@addOnSuccessListener

                allRecipes.clear()
                recipes.clear()

                for (document in documents) {
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val image_url = document.getString("image_url") ?: ""
                    val time = document.getLong("time")?.toInt() ?: 0
                    val category = document.getString("category") ?: ""
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
                        category = category,
                        is_official = isOfficial,
                        liked_by = liked_by,
                        bookmark_by = bookmark_by
                    )

                    if (category == categoryName) {
                        allRecipes.add(recipe)
                    }
                }

                filterRecipes(binding.etSearch.text.toString().trim())
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun filterRecipes(query: String) {
        recipes.clear()

        if (query.isEmpty()) {
            recipes.addAll(allRecipes)
        } else {
            val filtered = allRecipes.filter {
                it.title.contains(query, ignoreCase = true)
            }
            recipes.addAll(filtered)
        }

        adapter.notifyDataSetChanged()
    }



    private fun setupChipListeners() {
        binding.chipBreakfast.setOnClickListener {
            updateCategory("Breakfast")
        }

        binding.chipLunch.setOnClickListener {
            updateCategory("Lunch")
        }

        binding.chipDinner.setOnClickListener {
            updateCategory("Dinner")
        }

        binding.chipSnack.setOnClickListener {
            updateCategory("Snack")
        }

//        binding.chipDessert.setOnClickListener {
//            updateCategory("Dessert")
//        }
    }


    private fun updateCategory(newCategory: String) {
        categoryName = newCategory

        binding.chipBreakfast.isChecked = false
        binding.chipLunch.isChecked = false
        binding.chipDinner.isChecked = false
        binding.chipSnack.isChecked = false
//        binding.chipDessert.isChecked = false

        when (categoryName) {
            "Breakfast" -> binding.chipBreakfast.isChecked = true
            "Lunch" -> binding.chipLunch.isChecked = true
            "Dinner" -> binding.chipDinner.isChecked = true
            "Snack" -> binding.chipSnack.isChecked = true
//            "Dessert" -> binding.chipDessert.isChecked = true
        }

        fetchRecipes()
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
                        adapter.notifyDataSetChanged()
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
                        adapter.notifyDataSetChanged()
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

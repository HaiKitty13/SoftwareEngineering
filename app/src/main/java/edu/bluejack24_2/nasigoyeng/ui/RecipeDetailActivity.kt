package edu.bluejack24_2.nasigoyeng.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.data.models.User
import edu.bluejack24_2.nasigoyeng.databinding.ActivityRecipeDetailBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.IngredientsRecipeDetailAdapter
import edu.bluejack24_2.nasigoyeng.ui.adapter.StepsRecipeDetailAdapter
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class RecipeDetailActivity : BaseActivity() {

    private lateinit var recipeId: String

    private lateinit var recipe: Recipe
    private lateinit var user: User
    
    private lateinit var binding: ActivityRecipeDetailBinding

    private lateinit var ingredientsAdapter : IngredientsRecipeDetailAdapter
    private lateinit var stepsAdapter : StepsRecipeDetailAdapter

    private lateinit var currentUserId : String

    private val database = FirebaseDatabase.getInstance("https://nasigoyeng-5c083-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        recipeId = intent?.getStringExtra("RECIPE_ID") ?: ""

        binding.profileLayout.setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("USER", recipe.created_by)
            startActivity(intent)
        }

        fetchRecipe()
        addToHistory()
    }

    private fun setupAdapter(){
        ingredientsAdapter = IngredientsRecipeDetailAdapter(recipe.ingredients)
        binding.rvIngredients.adapter = ingredientsAdapter
        binding.rvIngredients.layoutManager = LinearLayoutManager(this)
        binding.rvIngredients.isNestedScrollingEnabled = false

        stepsAdapter = StepsRecipeDetailAdapter(recipe.steps)
        binding.rvSteps.adapter = stepsAdapter
        binding.rvSteps.layoutManager = LinearLayoutManager(this)
        binding.rvSteps.isNestedScrollingEnabled = false


    }

    private fun fetchRecipe(){
        firestore.collection("recipes")
            .document(recipeId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val image_url = document.getString("image_url") ?: ""
                    val time = document.getLong("time")?.toInt() ?: 0
                    val category = document.getString("category") ?: ""
                    val calories = document.getLong("calories")?.toInt() ?: 0
                    val isOfficial = document.getBoolean("_official") ?: false
                    val liked_by = document.get("liked_by") as? List<String> ?: emptyList()
                    val bookmark_by = document.get("bookmark_by") as? List<String> ?: emptyList()
                    val ingredients = document.get("ingredients") as? List<String> ?: emptyList()
                    val steps = document.get("steps") as? List<String> ?: emptyList()
                    val cretated_by = document.getString("created_by") ?: ""

                    recipe = Recipe(
                        id = document.id,
                        time = time,
                        image_url = image_url,
                        created_by = cretated_by,
                        title = title,
                        description = description,
                        country = "",
                        category = category,
                        calories = calories,
                        is_official = isOfficial,
                        liked_by = liked_by,
                        bookmark_by = bookmark_by,
                        ingredients = ingredients,
                        steps = steps
                    )

                    Log.d("RecipeDetailActivity", "Recipe fetched: $recipe")

                    setupAdapter()
                    fetchUser(recipe.created_by)

                }
            }
            .addOnFailureListener { e ->

            }
    }

    private fun fetchUser(cretated_by: String){

        firestore.collection("users")
            .document(cretated_by)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: ""
                    val name = document.getString("name") ?: ""
                    val profile_picture = document.getString("profile_picture") ?: ""
                    val is_private = document.getBoolean("is_private") ?: false
                    val bio = document.getString("bio") ?: ""
                    val dob = document.getDate("dob") ?: ""
                    val followers = document.get("followers") as? List<String> ?: emptyList()
                    val following = document.get("following") as? List<String> ?: emptyList()
                    val mobile_phone = document.getString("mobile_phone") ?: ""

                    user = User(
                        id = currentUserId,
                        username = username,
                        name = name,
                        profile_picture = profile_picture,
                        is_private = is_private,
                        bio = bio,
                        followers = followers,
                        following = following,
                        mobile_phone = mobile_phone,
                        bookmark_recipes = emptyList(),
                        dob = Date(0),
                        height = 0.0,
                        weight = 0.0,
                        like_recipe = emptyList(),
                        posts = emptyList(),
                        like_post = emptyList(),
                        preferences = emptyList(),
                        allergies = emptyList()
                    )

                    setupUI()

                } else {
                    Log.e("RecipeDetailActivity", "User not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("RecipeDetailActivity", "Error fetching user: ${e.message}")
            }
    }

    private fun addToHistory(){
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val userHistoryRef = database.child("history").child(currentUserId)

        userHistoryRef.child(recipeId).setValue(System.currentTimeMillis())

        userHistoryRef.orderByValue().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val total = snapshot.childrenCount
                if (total > 10) {
                    val excess = total - 10
                    val iterator = snapshot.children.iterator()
                    repeat(excess.toInt()) {
                        if (iterator.hasNext()) {
                            iterator.next().ref.removeValue()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }


    private fun setupUI(){

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

        binding.recipeTitle.text = recipe.title
        binding.recipeDescription.text = recipe.description
        binding.tvTime.text = recipe.time.toString() + " min"
        binding.tvCalories.text = recipe.calories.toString() + " kcal"

        binding.createdBy.text = user.name
        binding.username.text = user.username

        Glide.with(binding.root.context)
            .load(recipe.image_url)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(binding.recipeImage)

        Glide.with(binding.root.context)
            .load(user.profile_picture)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(binding.profilePic)

        binding.btnBack.setOnClickListener{
            finish()
        }

        binding.imageBookmark.setOnClickListener {
            toggleBookmark(recipe)
        }

        binding.imageFavorite.setOnClickListener {
            toggleLike(recipe)
        }
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
            .addOnSuccessListener { Log.d("RecipeDetailActivity", "Recipe like updated") }
            .addOnFailureListener { e -> Log.e("RecipeDetailActivity", "Failed to update recipe like: ${e.message}") }

        userRef.update(userUpdate)
            .addOnSuccessListener { Log.d("RecipeDetailActivity", "User like updated") }
            .addOnFailureListener { e -> Log.e("RecipeDetailActivity", "Failed to update user like: ${e.message}") }

        setupUI()
    }


    private fun toggleBookmark(recipe: Recipe) {
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
            .addOnSuccessListener { Log.d("RecipeDetailActivity", "Recipe bookmark updated") }
            .addOnFailureListener { e -> Log.e("RecipeDetailActivity", "Failed to update recipe bookmark: ${e.message}") }

        userRef.update(userUpdate)
            .addOnSuccessListener { Log.d("RecipeDetailActivity", "User bookmark updated") }
            .addOnFailureListener { e -> Log.e("RecipeDetailActivity", "Failed to update user bookmark: ${e.message}") }

        setupUI()
    }

}   
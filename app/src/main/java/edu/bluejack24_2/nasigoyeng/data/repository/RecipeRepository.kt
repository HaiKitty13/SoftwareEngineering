package edu.bluejack24_2.nasigoyeng.data.repository

import com.google.firebase.auth.FirebaseAuth
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RecipeRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val recipeCollection = firestore.collection("recipes")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun saveRecipe(recipe: Recipe): Result<String>{
        return try{
            val docRef = recipeCollection.add(recipe).await()

            if(userId != null) {
                val userDocRef = firestore.collection("users").document(userId)
                userDocRef.update("my_recipe", com.google.firebase.firestore.FieldValue.arrayUnion(docRef.id)).await()
            }


            Result.success(docRef.id)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(recipeId: String, recipe: Recipe): Result<Unit>{
        return try {
            recipeCollection.document(recipeId).set(recipe).await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun getRecipe(recipeId: String): Result<Recipe>{
        return try{
            val document = recipeCollection.document(recipeId).get().await()
            val recipe = document.toObject(Recipe::class.java)?.copy(id = document.id)

            if (recipe != null){
                Result.success(recipe)
            } else {
                Result.failure(Exception("Recipe not found"))
            }
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}
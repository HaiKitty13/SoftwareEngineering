package edu.bluejack24_2.nasigoyeng.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.databinding.FragmentRecipeBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.RecipeAdapter
import com.google.firebase.firestore.FirebaseFirestore

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!
    private val TAG = "RecipeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            _binding = FragmentRecipeBinding.inflate(inflater, container, false)
            Log.d(TAG, "onCreateView: Binding created successfully")

            binding.recipeRv.layoutManager = GridLayoutManager(requireContext(), 3)
            binding.recipeRv.adapter = RecipeAdapter(emptyList())

            getRecipeData { recipes ->
                if (isAdded && context != null && _binding != null) {
                    try {
                        Log.d(TAG, "Received ${recipes.size} recipes")
                        val recipeAdapter = RecipeAdapter(recipes)
                        binding.recipeRv.adapter = recipeAdapter
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting adapter: ${e.message}", e)
                    }
                }
            }

            return binding.root

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreateView: ${e.message}", e)
            return null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }

    private fun getRecipeData(callback: (List<Recipe>) -> Unit) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userId = arguments?.getString("USER")

            Log.d(TAG, "Getting recipes created by userId: $userId")

            if (userId.isNullOrEmpty()) {
                Log.w(TAG, "UserId is null or empty")
                callback(emptyList())
                return
            }

            firestore.collection("recipes")
                .whereEqualTo("created_by", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!isAdded || context == null || _binding == null) {
                        Log.d(TAG, "Fragment not attached, skipping recipe processing")
                        return@addOnSuccessListener
                    }

                    val resultList = mutableListOf<Recipe>()

                    for (recipeDoc in querySnapshot) {
                        try {
                            val title = recipeDoc.getString("title") ?: ""
                            val description = recipeDoc.getString("description") ?: ""
                            val image_url = recipeDoc.getString("image_url") ?: ""
                            val time = recipeDoc.getLong("time")?.toInt() ?: 0

                            val recipe = Recipe(
                                id = recipeDoc.id,
                                time = time,
                                image_url = image_url,
                                created_by = recipeDoc.getString("created_by") ?: "",
                                steps = recipeDoc.get("steps") as? ArrayList<String> ?: arrayListOf(),
                                title = title,
                                description = description,
                                ingredients = recipeDoc.get("ingredients") as? ArrayList<String> ?: arrayListOf(),
                                country = recipeDoc.getString("country") ?: "",
                                category = recipeDoc.getString("category") ?: ""
                            )

                            resultList.add(recipe)
                            Log.d(TAG, "Added recipe: $title")

                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing recipe document: ${e.message}", e)
                        }
                    }

                    callback(resultList)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting user recipes: ${exception.message}", exception)
                    if (isAdded && context != null && _binding != null) {
                        callback(emptyList())
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error in getRecipeData: ${e.message}", e)
            callback(emptyList())
        }
    }

}
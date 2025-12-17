package edu.bluejack24_2.nasigoyeng.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.Recipe
import edu.bluejack24_2.nasigoyeng.databinding.FragmentRecipeBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.RecipeAdapter
import com.google.firebase.firestore.FirebaseFirestore

class LikesFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)

        getRecipeData { recipes ->
            if (isAdded && context != null) {
                val recipeAdapter = RecipeAdapter(recipes)
                binding.recipeRv.layoutManager = GridLayoutManager(requireContext(), 3)
                binding.recipeRv.adapter = recipeAdapter
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getRecipeData(callback: (List<Recipe>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = arguments?.getString("USER") ?: ""

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!isAdded || context == null) {
                    return@addOnSuccessListener
                }

                val likedRecipeIds = documents?.get("like_recipe") as? List<String> ?: emptyList()

                if (likedRecipeIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                val resultList = mutableListOf<Recipe>()
                var completedQueries = 0

                for (recipeId in likedRecipeIds) {
                    firestore.collection("recipes")
                        .document(recipeId)
                        .get()
                        .addOnSuccessListener { recipeDoc ->
                            if (!isAdded || context == null) {
                                return@addOnSuccessListener
                            }

                            if (recipeDoc.exists()) {
                                val title = recipeDoc.getString("title") ?: ""
                                val description = recipeDoc.getString("description") ?: ""
                                val image_url = recipeDoc.getString("image_url") ?: ""
                                val time = recipeDoc.getLong("time")?.toInt() ?: 0

                                val recipe = Recipe(
                                    id = recipeDoc.id,
                                    time = time,
                                    image_url = image_url,
                                    created_by = "",
                                    steps = arrayListOf(),
                                    title = title,
                                    description = description,
                                    ingredients = arrayListOf(),
                                    country = "",
                                    category = ""
                                )

                                resultList.add(recipe)
                            }

                            completedQueries++
                            if (completedQueries == likedRecipeIds.size) {
                                if (isAdded && context != null) {
                                    callback(resultList)
                                }
                            }
                        }
                        .addOnFailureListener {
                            completedQueries++
                            if (completedQueries == likedRecipeIds.size) {
                                if (isAdded && context != null) {
                                    callback(resultList)
                                }
                            }
                        }
                }
            }
            .addOnFailureListener {
                if (isAdded && context != null) {
                    callback(emptyList())
                }
            }
    }
}
package edu.bluejack24_2.nasigoyeng.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.Post
import edu.bluejack24_2.nasigoyeng.databinding.FragmentProfilePostBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.PostAdapter
import com.google.firebase.firestore.FirebaseFirestore

class ProfilePostFragment : Fragment() {
    private var _binding: FragmentProfilePostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfilePostBinding.inflate(inflater, container, false)

        getRecipeData { posts ->
            if (isAdded && context != null) {
                val postAdapter = PostAdapter(posts)
                binding.recipeRv.layoutManager = LinearLayoutManager(context)
                binding.recipeRv.adapter = postAdapter
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getRecipeData(callback: (List<Post>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = arguments?.getString("USER") ?: ""

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!isAdded || context == null) {
                    return@addOnSuccessListener
                }

                val likedRecipeIds = documents?.get("posts") as? List<String> ?: emptyList()

                if (likedRecipeIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                val resultList = mutableListOf<Post>()
                var completedQueries = 0

                for (recipeId in likedRecipeIds) {
                    firestore.collection("posts")
                        .document(recipeId)
                        .get()
                        .addOnSuccessListener { postDoc ->
                            if (!isAdded || context == null) {
                                return@addOnSuccessListener
                            }

                            if (postDoc.exists()) {
                                val description = postDoc.getString("description") ?: ""
                                val image_url = postDoc.getString("media") ?: ""

                                val post = Post(
                                    id = postDoc.id,
                                    media = image_url,
                                    description = description,
                                )

                                resultList.add(post)
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
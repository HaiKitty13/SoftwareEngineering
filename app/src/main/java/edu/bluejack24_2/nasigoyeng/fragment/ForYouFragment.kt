package edu.bluejack24_2.nasigoyeng.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.PostUser
import edu.bluejack24_2.nasigoyeng.databinding.FragmentForYouBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.PostExploreAdapter
import com.google.firebase.firestore.FirebaseFirestore

class ForYouFragment : Fragment() {

    private var _binding: FragmentForYouBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForYouBinding.inflate(inflater, container, false)

        getPostData { posts ->
            if (isAdded && context != null) {
                val postAdapter = PostExploreAdapter(posts)
                binding.postRv.layoutManager = LinearLayoutManager(context)
                binding.postRv.adapter = postAdapter
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getPostData(callback: (List<PostUser>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val resultList = mutableListOf<PostUser>()
                var completed = 0

                if (documents.isEmpty) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                for (postDoc in documents) {
                    val description = postDoc.getString("description") ?: ""
                    val image_url = postDoc.getString("media") ?: ""
                    val user_id = postDoc.getString("user_id") ?: ""

                    firestore.collection("users")
                        .document(user_id)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                val username = userDoc.getString("username") ?: ""
                                val name =  userDoc.getString("name") ?: ""
                                val profilePicture = userDoc.getString("profile_picture") ?: ""

                                val post = PostUser(
                                    id = postDoc.id,
                                    media = image_url,
                                    description = description,
                                    profile_picture = profilePicture,
                                    name = name,
                                    user_id = user_id,
                                    username = username
                                )
                                resultList.add(post)
                            }
                            completed++
                            if (completed == documents.size()) {
                                callback(resultList)
                            }
                        }
                        .addOnFailureListener {
                            completed++
                            if (completed == documents.size()) {
                                callback(resultList)
                            }
                        }
                }
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}
package edu.bluejack24_2.nasigoyeng.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.Post
import edu.bluejack24_2.nasigoyeng.data.models.PostUser
import edu.bluejack24_2.nasigoyeng.databinding.FragmentFollowingPostBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.PostAdapter
import edu.bluejack24_2.nasigoyeng.ui.adapter.PostExploreAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FollowingPostFragment : Fragment() {
    private lateinit var binding: FragmentFollowingPostBinding
    private lateinit var adapter: PostAdapter
    private val postList = ArrayList<Post>()
    private val followedUserIds = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowingPostBinding.inflate(inflater, container, false)
        getPostData { posts ->
            if (isAdded && context != null) {
                val postAdapter = PostExploreAdapter(posts)
                binding.postRv.layoutManager = LinearLayoutManager(context)
                binding.postRv.adapter = postAdapter
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostAdapter(postList)
        binding.postRv.layoutManager = LinearLayoutManager(context)
        binding.postRv.adapter = adapter
    }

    private fun getPostData(callback: (List<PostUser>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { userDoc ->
                val followingList = userDoc.get("following") as? List<String> ?: emptyList()

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
                            val user_id = postDoc.getString("user_id") ?: ""

                            // Filter: hanya ambil post dari user yang di-follow
                            if (!followingList.contains(user_id)) {
                                completed++
                                if (completed == documents.size()) {
                                    callback(resultList)
                                }
                                continue
                            }

                            val description = postDoc.getString("description") ?: ""
                            val image_url = postDoc.getString("media") ?: ""

                            firestore.collection("users")
                                .document(user_id)
                                .get()
                                .addOnSuccessListener { userDetail ->
                                    if (userDetail.exists()) {
                                        val username = userDetail.getString("username") ?: ""
                                        val name = userDetail.getString("name") ?: ""
                                        val profilePicture = userDetail.getString("profile_picture") ?: ""

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
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}

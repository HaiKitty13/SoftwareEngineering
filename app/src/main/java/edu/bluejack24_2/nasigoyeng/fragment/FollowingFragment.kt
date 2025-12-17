package edu.bluejack24_2.nasigoyeng.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.User
import edu.bluejack24_2.nasigoyeng.databinding.FragmentFollowingBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.ProfileUserFollowingAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class FollowingFragment : Fragment() {
    private lateinit var binding: FragmentFollowingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowingBinding.inflate(inflater, container, false)

        refresh()

        return binding.root
    }

    private fun refresh(){
        getUserData { users ->
            val userAdapter = ProfileUserFollowingAdapter(users) { userId ->
                onUserClick(userId)
            }
            binding.followingRv.layoutManager = LinearLayoutManager(context)
            binding.followingRv.adapter = userAdapter
        }
    }

    private fun getUserData(callback: (List<User>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .whereEqualTo("id", currentUser)
            .get()
            .addOnSuccessListener { documents ->
                val document = documents.firstOrNull()
                val followers = document?.get("following") as? List<String> ?: emptyList()

                if (followers.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                val chunks = followers.chunked(10)
                val resultList = mutableListOf<User>()
                var completedChunks = 0

                for (chunk in chunks) {
                    firestore.collection("users")
                        .whereIn("id", chunk)
                        .get()
                        .addOnSuccessListener { followerDocs ->
                            for (followerDoc in followerDocs) {
                                val profilePicture = followerDoc.getString("profile_picture") ?: ""
                                val username = followerDoc.getString("username") ?: ""
                                val name = followerDoc.getString("name") ?: ""
                                val dump = ""
                                val dumpbool = false

                                val user = User(
                                    id = followerDoc.id,
                                    username = username,
                                    profile_picture = profilePicture,
                                    name = name,
                                    is_private = dumpbool,
                                    bio = dump,
                                    bookmark_recipes = arrayListOf(),
                                    dob = Date(),
                                    height = 0.0,
                                    weight = 0.0,
                                    followers = arrayListOf(),
                                    following = arrayListOf(),
                                    like_recipe = arrayListOf(),
                                    mobile_phone = dump,
                                    posts = arrayListOf(),
                                    like_post = arrayListOf(),
                                    preferences = arrayListOf(),
                                    allergies = arrayListOf()
                                )

                                resultList.add(user)
                            }

                            completedChunks++
                            if (completedChunks == chunks.size) {
                                callback(resultList)
                            }
                        }
                        .addOnFailureListener {
                            completedChunks++
                            if (completedChunks == chunks.size) {
                                callback(resultList)
                            }
                        }
                }
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    private fun onUserClick(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid.toString()

        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { currentUserDoc ->
                val currentFollowers = currentUserDoc.get("following") as? MutableList<String> ?: mutableListOf()

                if (currentFollowers.contains(userId)) {
                    currentFollowers.remove(userId)

                    db.collection("users")
                        .document(currentUserId)
                        .update("following", currentFollowers)
                        .addOnSuccessListener {
                            refresh()
                        }
                }
            }

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { targetUserDoc ->
                val targetFollowers = targetUserDoc.get("followers") as? MutableList<String> ?: mutableListOf()
                targetFollowers.remove(currentUserId)
                db.collection("users")
                    .document(userId)
                    .update("followers", targetFollowers)
            }
    }
}
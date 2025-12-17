package edu.bluejack24_2.nasigoyeng.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.User
import edu.bluejack24_2.nasigoyeng.databinding.FragmentFollowerBinding
import edu.bluejack24_2.nasigoyeng.ui.adapter.ProfileUserFollowersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class FollowerFragment : Fragment() {

    private lateinit var binding: FragmentFollowerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowerBinding.inflate(inflater, container, false)

        refresh()

        return binding.root
    }

    private fun refresh(){
        getUserData { users ->
            val userAdapter = ProfileUserFollowersAdapter(users) { userId ->
                onUserClick(userId)
            }
            binding.followerRv.layoutManager = LinearLayoutManager(context)
            binding.followerRv.adapter = userAdapter
        }
    }

    private fun getUserData(callback: (List<User>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val users = arrayListOf<User>()
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .whereEqualTo("id", currentUser)
            .get()
            .addOnSuccessListener { documents ->
                val document = documents.firstOrNull()
                val followers = document?.get("followers") as? List<String> ?: emptyList()

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
                val currentFollowers = currentUserDoc.get("followers") as? MutableList<String> ?: mutableListOf()

                if (currentFollowers.contains(userId)) {
                    currentFollowers.remove(userId)

                    db.collection("users")
                        .document(currentUserId)
                        .update("followers", currentFollowers)
                        .addOnSuccessListener {
                            refresh()
                        }
                }
            }

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { targetUserDoc ->
                val targetFollowers = targetUserDoc.get("following") as? MutableList<String> ?: mutableListOf()
                targetFollowers.remove(currentUserId)
                db.collection("users")
                    .document(userId)
                    .update("following", targetFollowers)
            }
    }
}

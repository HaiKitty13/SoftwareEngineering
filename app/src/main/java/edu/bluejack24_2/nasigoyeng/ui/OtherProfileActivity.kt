package edu.bluejack24_2.nasigoyeng.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.databinding.ActivityOtherProfileBinding
import edu.bluejack24_2.nasigoyeng.fragment.LikesFragment
import edu.bluejack24_2.nasigoyeng.fragment.ProfilePostFragment
import edu.bluejack24_2.nasigoyeng.fragment.RecipeFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OtherProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtherProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var isPrivateAccount = false
    private var isFollowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("USER") ?: ""
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUserId = firebaseAuth.currentUser?.uid ?: ""

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val username = document.getString("username") ?: ""
                    isPrivateAccount = document.getBoolean("_private") ?: false

                    binding.username.text = "@$username"

                    binding.tabLayout.removeAllTabs()

                    if (!isPrivateAccount || userId == currentUserId) {
                        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Recipe"))
                        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Posts"))
                        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Likes Recipe"))

                        switchFragment(RecipeFragment(), userId)

                        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                            override fun onTabSelected(tab: TabLayout.Tab) {
                                when (tab.position) {
                                    0 -> switchFragment(RecipeFragment(), userId)
                                    1 -> switchFragment(ProfilePostFragment(), userId)
                                    2 -> switchFragment(LikesFragment(), userId)
                                }
                            }

                            override fun onTabUnselected(tab: TabLayout.Tab) {}
                            override fun onTabReselected(tab: TabLayout.Tab) {}
                        })
                    } else {
                        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("This account is private"))
                        Toast.makeText(this, "Akun ini bersifat privat", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }

        binding.backArrow.setOnClickListener {
            finish()
        }

        binding.followBtn.setOnClickListener {
            if (currentUserId.isNotEmpty() && userId.isNotEmpty()) {
                if (isFollowing) {
                    unfollowUser(currentUserId, userId)
                } else {
                    followUser(currentUserId, userId)
                }
            }
        }

        db.collection("users")
            .whereEqualTo("id", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val followers = document.get("followers") as? List<String> ?: emptyList()
                    val following = document.get("following") as? List<String> ?: emptyList()
                    val my_recipe = document.get("my_recipe") as? List<String> ?: emptyList()

                    binding.recipesCount.text = my_recipe.size.toString()
                    binding.followersCount.text = followers.size.toString()
                    binding.followingCount.text = following.size.toString()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mengambil data follower: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun switchFragment(fragment: Fragment, userId: String) {
        val bundle = Bundle()
        bundle.putString("USER", userId)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private fun followUser(currentUserId: String, targetUserId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { currentUserDoc ->
                val currentFollowing = currentUserDoc.get("following") as? MutableList<String> ?: mutableListOf()

                if (!currentFollowing.contains(targetUserId)) {
                    currentFollowing.add(targetUserId)

                    db.collection("users")
                        .document(currentUserId)
                        .update("following", currentFollowing)
                        .addOnSuccessListener {
                            updateTargetUserFollowers(targetUserId, currentUserId, true)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal follow user", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unfollowUser(currentUserId: String, targetUserId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { currentUserDoc ->
                val currentFollowing = currentUserDoc.get("following") as? MutableList<String> ?: mutableListOf()

                if (currentFollowing.contains(targetUserId)) {
                    currentFollowing.remove(targetUserId)

                    db.collection("users")
                        .document(currentUserId)
                        .update("following", currentFollowing)
                        .addOnSuccessListener {
                            updateTargetUserFollowers(targetUserId, currentUserId, false)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal unfollow user", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTargetUserFollowers(targetUserId: String, currentUserId: String, isFollow: Boolean) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(targetUserId)
            .get()
            .addOnSuccessListener { targetUserDoc ->
                val targetFollowers = targetUserDoc.get("followers") as? MutableList<String> ?: mutableListOf()

                if (isFollow) {
                    if (!targetFollowers.contains(currentUserId)) {
                        targetFollowers.add(currentUserId)
                    }
                } else {
                    targetFollowers.remove(currentUserId)
                }

                db.collection("users")
                    .document(targetUserId)
                    .update("followers", targetFollowers)
                    .addOnSuccessListener {
                        isFollowing = isFollow
                        updateFollowButton()
                        val message = if (isFollow) "Berhasil mengikuti user" else "Berhasil berhenti mengikuti user"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal update followers", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data target user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFollowButton() {
        if (isFollowing) {
            binding.followBtn.text = "Unfollow"
        } else {
            binding.followBtn.text = "Follow"
        }
    }
}

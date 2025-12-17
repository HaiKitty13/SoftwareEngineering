package edu.bluejack24_2.nasigoyeng.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.databinding.FragmentProfileBinding
import edu.bluejack24_2.nasigoyeng.ui.EditProfileActivity
import edu.bluejack24_2.nasigoyeng.ui.FollowActivity
import edu.bluejack24_2.nasigoyeng.ui.setting.SettingsActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show()
            return
        }

        val usernameText = binding.username
        tabLayout = binding.tabLayout

        switchFragment(RecipeFragment(), currentUser.uid)

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("id", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val username = document.getString("username") ?: currentUser.uid
                    usernameText.text = "@$username"
                    binding.name.text = username

                    tabLayout.removeAllTabs()

                    tabLayout.addTab(tabLayout.newTab().setText("Recipe"))
                    tabLayout.addTab(tabLayout.newTab().setText("Likes"))
                    tabLayout.addTab(tabLayout.newTab().setText("Bookmarks"))
                    tabLayout.addTab(tabLayout.newTab().setText("Posts"))
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Gagal mengambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val userId = firebaseAuth.currentUser?.uid ?: return
                when (tab.position) {
                    0 -> switchFragment(RecipeFragment(), userId)
                    1 -> switchFragment(LikesFragment(), userId)
                    2 -> switchFragment(BookmarksFragment(), userId)
                    3 -> switchFragment(ProfilePostFragment(), userId)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.followingLayout.setOnClickListener {
            val intent = Intent(requireContext(), FollowActivity::class.java)
            startActivity(intent)
        }

        binding.followersLayout.setOnClickListener {
            val intent = Intent(requireContext(), FollowActivity::class.java)
            startActivity(intent)
        }

        db.collection("users")
            .whereEqualTo("id", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val followers = document.get("followers") as? List<String> ?: emptyList()
                    val following = document.get("following") as? List<String> ?: emptyList()
                    val recipe_count = document.get("my_recipe") as? List<String> ?: emptyList()

                    binding.recipesCount.text = recipe_count.size.toString()
                    binding.followersCount.text = followers.size.toString()
                    binding.followingCount.text = following.size.toString()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Gagal mengambil data follower: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
            Toast.makeText(requireContext(), "Click Edit Profile Button", Toast.LENGTH_SHORT).show()
        }

        binding.btnSettingProfile.setOnClickListener{
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun switchFragment(fragment: Fragment, userId: String) {
        if (!isAdded || activity == null) {
            return
        }

        val bundle = Bundle()
        bundle.putString("USER", userId)
        fragment.arguments = bundle

        childFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
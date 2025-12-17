package edu.bluejack24_2.nasigoyeng.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.databinding.ActivityFollowBinding
import edu.bluejack24_2.nasigoyeng.fragment.FollowerFragment
import edu.bluejack24_2.nasigoyeng.fragment.FollowingFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FollowActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFollowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        switchFragment(FollowerFragment())

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("id", currentUser)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val username = document.getString("username") ?: currentUser
                    val followers = document.get("followers") as? List<String> ?: emptyList()
                    val following = document.get("following") as? List<String> ?: emptyList()

                    binding.usernameText.text = "@" + username
                    binding.tabLayout.addTab(binding.tabLayout.newTab().setText("${followers.size} Followers"))
                    binding.tabLayout.addTab(binding.tabLayout.newTab().setText("${following.size} Following"))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@FollowActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> switchFragment(FollowerFragment())
                    1 -> switchFragment(FollowingFragment())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.backArrow.setOnClickListener {
            finish()
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}

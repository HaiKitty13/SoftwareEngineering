package edu.bluejack24_2.nasigoyeng.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.databinding.ActivityMainBinding
import edu.bluejack24_2.nasigoyeng.fragment.AdminFragment
import edu.bluejack24_2.nasigoyeng.fragment.ExploreFragment
import edu.bluejack24_2.nasigoyeng.fragment.HomeFragment
import edu.bluejack24_2.nasigoyeng.fragment.ProfileFragment
import edu.bluejack24_2.nasigoyeng.ui.addrecipe.AddRecipeActivity
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity
import edu.bluejack24_2.nasigoyeng.ui.message.MessageListFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isAdmin = intent.getBooleanExtra("is_admin", false)

        if (auth.currentUser == null) {
            Log.w("MainActivity", "No user is logged in.")
            return
        }

        switchFragment(if (isAdmin) AdminFragment() else HomeFragment())
        setupBottomNavigation()

    }

    private fun setupBottomNavigation() {
        binding.bottomNavbar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_item -> {
                    switchFragment(if (isAdmin) AdminFragment() else HomeFragment())
                }
                R.id.profile_item -> {
                    switchFragment(ProfileFragment())
                }
                R.id.chat_item -> {
                    switchFragment(MessageListFragment())
                }
                R.id.explore_item -> {
                    switchFragment(ExploreFragment())
                }
                R.id.add_recipe -> {
                    val intent = Intent(this, AddRecipeActivity::class.java)
                    intent.putExtra("is_admin", isAdmin)
                    startActivity(intent)
                }
            }
            true
        }
    }


    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}

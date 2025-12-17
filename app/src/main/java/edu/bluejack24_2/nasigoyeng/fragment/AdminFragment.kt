package edu.bluejack24_2.nasigoyeng.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import edu.bluejack24_2.nasigoyeng.data.models.User
import edu.bluejack24_2.nasigoyeng.databinding.FragmentAdminBinding
import edu.bluejack24_2.nasigoyeng.ui.OtherProfileActivity
import edu.bluejack24_2.nasigoyeng.ui.adapter.ProfileUserAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class AdminFragment : Fragment() {

    private lateinit var binding: FragmentAdminBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminBinding.inflate(inflater, container, false)

        getUserData { users ->
            val userAdapter = ProfileUserAdapter(users) { userId ->
                onUserClick(userId)
            }
            binding.adminRv.layoutManager = LinearLayoutManager(context)
            binding.adminRv.adapter = userAdapter
        }

        return binding.root
    }

    private fun getUserData(callback: (List<User>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val users = arrayListOf<User>()

        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val profilePicture = document.getString("profile_picture") ?: ""
                    val username = document.getString("username") ?: ""
                    val name = document.getString("name") ?: ""
                    val dump = ""
                    val dumpbool = false

                    val user = User(
                        id = document.id,
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

                    users.add(user)
                }
                callback(users)
            }
    }

    private fun onUserClick(userId: String) {
        val intent = Intent(requireContext(), OtherProfileActivity::class.java)
        intent.putExtra("USER", userId)
        startActivity(intent)
    }
}
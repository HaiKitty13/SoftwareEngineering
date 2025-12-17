package edu.bluejack24_2.nasigoyeng.ui

import android.os.Bundle
import android.widget.Toast
import edu.bluejack24_2.nasigoyeng.data.models.User
import edu.bluejack24_2.nasigoyeng.databinding.ActivityEditProfileBinding
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class EditProfileActivity : BaseActivity() {
    private lateinit var binding : ActivityEditProfileBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        getUserData()

        binding.btnSave.setOnClickListener {
            updateUserProfile()
        }

        binding.changePassword.setOnClickListener {
            updatePasswordUser()
        }
    }

    private fun getUserData() {
        val firestore = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .whereEqualTo("id", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents.first() // ambil dokumen pertama (harusnya 1 saja)

                    val user = User(
                        id = doc.id,
                        username = doc.getString("username") ?: "",
                        profile_picture = doc.getString("profile_picture") ?: "",
                        name = doc.getString("name") ?: "",
                        is_private = doc.getBoolean("is_private") ?: false,
                        bio = doc.getString("bio") ?: "",
                        bookmark_recipes = doc.get("bookmark_recipes") as? ArrayList<String> ?: arrayListOf(),
                        dob = doc.getDate("dob") ?: Date(),
                        height = doc.getDouble("height") ?: 0.0,
                        weight = doc.getDouble("weight") ?: 0.0,
                        followers = doc.get("followers") as? ArrayList<String> ?: arrayListOf(),
                        following = doc.get("following") as? ArrayList<String> ?: arrayListOf(),
                        like_recipe = doc.get("like_recipe") as? ArrayList<String> ?: arrayListOf(),
                        mobile_phone = doc.getString("mobile_phone") ?: "",
                        posts = doc.get("posts") as? ArrayList<String> ?: arrayListOf(),
                        like_post = doc.get("like_post") as? ArrayList<String> ?: arrayListOf(),
                        allergies = doc.get("allergies") as? ArrayList<String> ?: arrayListOf(),
                        preferences = doc.get("preferences") as? ArrayList<String> ?: arrayListOf()

                    )

                    binding.user = user
                } else {
                    Toast.makeText(this, "User data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateUserProfile() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return

        val updatedName = binding.etName.text.toString().trim()
        val updatedUsername = binding.etUsername.text.toString().trim()
        val updatedBio = binding.etBio.text.toString().trim()

        if (updatedName.isEmpty() || updatedUsername.isEmpty()) {
            Toast.makeText(this, "Name dan Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "name" to updatedName,
            "username" to updatedUsername,
            "bio" to updatedBio
        )

        firebaseFirestore.collection("users")
            .whereEqualTo("id", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val docId = documents.documents.first().id
                    firebaseFirestore.collection("users")
                        .document(docId)
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Dokumen user tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePasswordUser() {
        val currentUser = firebaseAuth.currentUser
        val email = currentUser?.email

        if (email != null) {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset password email sent to $email", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send reset email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
        }
    }

}
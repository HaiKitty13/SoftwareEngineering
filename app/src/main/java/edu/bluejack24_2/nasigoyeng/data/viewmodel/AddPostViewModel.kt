package edu.bluejack24_2.nasigoyeng.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack24_2.nasigoyeng.data.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AddPostViewModel(
    private val supabase: SupabaseClient,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _imageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _imageUrl
    private var isUploading = false

    fun uploadImage(file: File) {
        viewModelScope.launch {
            try {
                val fileName = "post_image/${UUID.randomUUID()}.jpg"
                supabase.storage.from("nasigoyengenak").upload(
                    path = fileName,
                    file = file,
                    upsert = true
                )
                val imageUrl = supabase.storage.from("nasigoyengenak").publicUrl(fileName)
                _imageUrl.value = imageUrl
            } catch (e: Exception) {
                Log.e("SupabaseUpload", "Error uploading image: ${e.message}", e)
            }
        }
    }

    fun uploadPost(
        description: String,
        imageUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isUploading) return

        isUploading = true

        if (description.isEmpty() || imageUrl.isEmpty()) {
            onError("Please fill all the fields")
            isUploading = false
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onError("User not logged in")
            isUploading = false
            return
        }

        val userId = currentUser.uid
        val postId = UUID.randomUUID().toString()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->

                val postData = Post(
                    id = postId,
                    user_id = userId,
                    description = description,
                    media = imageUrl
                )

                firestore.collection("posts").document(postId)
                    .set(postData)
                    .addOnSuccessListener {
                        firestore.collection("users").document(userId)
                            .update("posts", com.google.firebase.firestore.FieldValue.arrayUnion(postId))
                            .addOnSuccessListener {
                                onSuccess()
                                isUploading = false
                            }
                            .addOnFailureListener { e ->
                                onError("Post uploaded but failed to update user posts: ${e.message}")
                                isUploading = false
                            }
                    }

                    .addOnFailureListener { e ->
                        onError("Failed to upload post: ${e.message}")
                        isUploading = false
                    }

            }
            .addOnFailureListener { e ->
                onError("Failed to retrieve user info: ${e.message}")
                isUploading = false
            }
    }

}
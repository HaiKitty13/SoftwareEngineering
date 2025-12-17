package edu.bluejack24_2.nasigoyeng.data.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack24_2.nasigoyeng.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RegisterViewModel(
    private val supabase: SupabaseClient,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl
    private var isRegistering = false

    fun uploadImage(file: File) {
        viewModelScope.launch {
            try {
                val fileName = "profile_pictures/${UUID.randomUUID()}.jpg"
                supabase.storage.from("nasigoyengenak").upload(
                    path = fileName,
                    file = file,
                    upsert = true
                )
                val imageUrl = supabase.storage.from("nasigoyengenak").publicUrl(fileName)
                _profileImageUrl.value = imageUrl
            } catch (e: Exception) {
                Log.e("SupabaseUpload", "Error uploading image: ${e.message}", e)
            }
        }
    }

    fun checkInput(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        dob: String,
        height: Double,
        weight: Double,
        confirmPassword: String,
        onError: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        if (fullName.isEmpty() || email.isEmpty() || mobileNumber.isEmpty() || dob.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || height.toString().isEmpty() || weight.toString().isEmpty()) {
            onError("Please fill all the fields")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Invalid email format")
            return
        }

        if (password.length < 8) {
            onError("Password length must be more than 8 characters")
            return
        }

        if (!password.any { it.isUpperCase() }) {
            onError("Password must have at least 1 uppercase")
            return
        }

        if (!password.any { it.isDigit() } || !password.any { it.isLetter() }) {
            onError("Password must be alphanumeric")
            return
        }

        if (password != confirmPassword) {
            onError("Password and confirm password do not match")
            return
        }

        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        var parsedDate: Date? = null

        try {
            parsedDate = dateFormat.parse(dob)
        } catch (e: Exception) {
            onError("Invalid date input (ddmmyyyy)")
            return
        }

        if (parsedDate == null) {
            onError("Date cannot be null")
            return
        }
        onSuccess("yey")
    }

    fun registerUser(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        imageUrl: String,
        dob: String,
        height: Double,
        weight: Double,
        preferences: ArrayList<String>,
        allergies: ArrayList<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isRegistering) return

        isRegistering = true
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        var parsedDate: Date? = null

        try {
            parsedDate = dateFormat.parse(dob)
        } catch (e: Exception) {
            onError("Invalid date input (dd-mm-yyy)")
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener

                val user = User(
                    id = userId,
                    username = fullName,
                    name = fullName,
                    profile_picture = imageUrl,
                    is_private = false,
                    bio = "",
                    bookmark_recipes = listOf(),
                    dob = parsedDate,
                    height = height,
                    weight = weight,
                    followers = listOf(),
                    following = listOf(),
                    like_recipe = listOf(),
                    mobile_phone = mobileNumber,
                    posts = listOf(),
                    like_post = listOf(),
                    preferences = preferences,
                    allergies = allergies,
                    my_recipe = listOf()
                )

                firestore.collection("users").document(userId)
                    .set(user)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Failed to save user") }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Registration failed")
            }
    }
}
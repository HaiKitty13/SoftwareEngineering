    package edu.bluejack24_2.nasigoyeng.data.viewmodel

    import android.util.Patterns
    import androidx.lifecycle.ViewModel
    import com.google.firebase.auth.FirebaseAuth

    class ResetPasswordViewModel (private val firebaseAuth: FirebaseAuth) :ViewModel() {
        private var isRegistering = false

        fun resetPassword(
            email: String,
            password: String,
            confirmPassword: String,
            onError: (String) -> Unit,
            onSuccess: (String) -> Unit,
        ) {
            if (isRegistering) return

            isRegistering = true

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                onError("Please fill all the fields")
                isRegistering = false
                return
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                onError("Invalid email format")
                isRegistering = false
                return
            }

            if (password.length < 8) {
                onError("Password length must be more than 8 characters")
                isRegistering = false
                return
            }

            if (!password.any { it.isUpperCase() }) {
                onError("Password must have at least 1 uppercase")
                isRegistering = false
                return
            }

            if (!password.any { it.isDigit() } || !password.any { it.isLetter() }) {
                onError("Password must be alphanumeric")
                isRegistering = false
                return
            }

            if (password != confirmPassword) {
                onError("Password and confirm password do not match")
                isRegistering = false
                return
            }

            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                currentUser.updatePassword(password)
                    .addOnSuccessListener {
                        isRegistering = false
                        onSuccess("Password successfully updated.")
                    }
                    .addOnFailureListener { exception ->
                        isRegistering = false
                        onError("Failed to update password: ${exception.message}")
                    }
            } else {
                isRegistering = false
                onError("No user is currently signed in.")
            }
        }
    }
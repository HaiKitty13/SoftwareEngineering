package edu.bluejack24_2.nasigoyeng.data.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel : ViewModel() {
    fun sendOtpEmail(
        email: String,
        onError: (String) -> Unit
    ) {
        if (email.isEmpty()) {
            onError("Email tidak boleh kosong")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Format email tidak valid")
            return
        }

        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.sendPasswordResetEmail(email)
    }
}

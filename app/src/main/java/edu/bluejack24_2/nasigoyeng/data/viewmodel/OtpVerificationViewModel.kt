package edu.bluejack24_2.nasigoyeng.data.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import edu.bluejack24_2.nasigoyeng.data.utils.sendotp

class OtpVerificationViewModel : ViewModel() {
    fun sendOtpEmail(
        email: String,
        onSuccess: (String) -> Unit,
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

        val otpCode = (100000..999999).random().toString()

        sendotp.saveOtpToFirestore(email, otpCode)

        sendotp.sendOtpViaGmail(
            emailRecipient = email,
            otpCode = otpCode,
            onResult = { success, message ->
                if (success) {
                    onSuccess(otpCode)
                } else {
                    onError(message)
                }
            }
        )
    }

    fun verifyOtp(
        email: String,
        userInputOtp: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (userInputOtp.isEmpty()) {
            onError("Kode OTP tidak boleh kosong")
            return
        }

        if (userInputOtp.length != 6) {
            onError("Kode OTP harus 6 digit")
            return
        }

        sendotp.verifyOtp(email, userInputOtp) { isValid ->
            if (isValid) {
                onSuccess()
            } else {
                onError("Kode OTP tidak valid atau sudah kedaluwarsa")
            }
        }
    }
}
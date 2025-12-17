package edu.bluejack24_2.nasigoyeng.data.utils

import com.google.firebase.firestore.FirebaseFirestore
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object sendotp{
    fun saveOtpToFirestore(email: String, otp: String) {
        val data = hashMapOf(
            "otp" to otp,
            "timestamp" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance()
            .collection("email_otps")
            .document(email)
            .set(data)
    }

    fun sendOtpViaGmail(
        emailRecipient: String,
        otpCode: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val senderEmail = "nasigoyeng.official@gmail.com"
        val senderPassword = "rzhk xepc fajj vgrc"

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailRecipient))
                subject = "Kode OTP Anda"
                setText("Kode OTP Anda adalah: $otpCode")
            }

            Thread {
                try {
                    Transport.send(message)
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onResult(true, "OTP berhasil dikirim")
                    }
                } catch (e: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onResult(false, "Gagal kirim email: ${e.message}")
                    }
                }
            }.start()
        } catch (e: Exception) {
            // Sudah di main thread karena try-catch ini bukan di dalam Thread
            onResult(false, "Kesalahan membuat email: ${e.message}")
        }
    }

    fun verifyOtp(email: String, userInputOtp: String, onResult: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("email_otps")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                val otp = document.getString("otp")
                val timestamp = document.getLong("timestamp") ?: 0L
                val currentTime = System.currentTimeMillis()

                val isExpired = currentTime - timestamp > 5 * 60 * 1000
                if (otp == userInputOtp && !isExpired) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
    }
}
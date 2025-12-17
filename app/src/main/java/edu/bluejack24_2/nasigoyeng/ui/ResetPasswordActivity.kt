package edu.bluejack24_2.nasigoyeng.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.data.viewmodel.ResetPasswordViewModel
import edu.bluejack24_2.nasigoyeng.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var email: String
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewHolder: ResetPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewHolder = ResetPasswordViewModel(FirebaseAuth.getInstance())
        email = intent.getStringExtra("EMAIL") ?: ""

        binding.continueButton.setOnClickListener {
            val password = binding.passwordTb.text.toString()
            val confirmPassword = binding.confirmPasswordTb.text.toString()
            viewHolder.resetPassword(
                email,
                password,
                confirmPassword,
                onSuccess = {
                    Toast.makeText(this, "Reset passowrd berhasil diverifikasi", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onError = { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
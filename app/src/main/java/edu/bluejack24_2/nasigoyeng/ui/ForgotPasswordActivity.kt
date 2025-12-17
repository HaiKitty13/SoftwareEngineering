package edu.bluejack24_2.nasigoyeng.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.data.viewmodel.ForgotPasswordViewModel
import edu.bluejack24_2.nasigoyeng.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ForgotPasswordViewModel()

        binding.continueButton.setOnClickListener {
            val email = binding.emailTb.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.sendOtpEmail(
                email,
                onError = {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
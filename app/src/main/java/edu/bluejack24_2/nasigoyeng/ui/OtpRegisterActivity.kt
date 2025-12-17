package edu.bluejack24_2.nasigoyeng.ui

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.data.viewmodel.OtpVerificationViewModel
import edu.bluejack24_2.nasigoyeng.databinding.ActivityOtpRegisterBinding

class OtpRegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpRegisterBinding
    private lateinit var viewModel: OtpVerificationViewModel
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = OtpVerificationViewModel()

        email = intent.getStringExtra("EMAIL").toString()

        viewModel.sendOtpEmail(
            email,
            onSuccess = {
                Toast.makeText(this, "OTP telah dikirim ke email", Toast.LENGTH_SHORT).show()
            },
            onError = {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        )

        binding.otp1.setupOtpInput(binding.otp2)
        binding.otp2.setupOtpInput(binding.otp3)
        binding.otp3.setupOtpInput(binding.otp4)
        binding.otp4.setupOtpInput(binding.otp5)
        binding.otp5.setupOtpInput(binding.otp6)
        binding.otp6.setupOtpInput(null)

        binding.continueButton.setOnClickListener {
            val otpCode = binding.otp1.text.toString() +
                    binding.otp2.text.toString() +
                    binding.otp3.text.toString() +
                    binding.otp4.text.toString() +
                    binding.otp5.text.toString() +
                    binding.otp6.text.toString()

            viewModel.verifyOtp(
                email = email,
                userInputOtp = otpCode,
                onSuccess = {
                    Toast.makeText(this, "OTP berhasil diverifikasi", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                },
                onError = { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.resendCode.setOnClickListener {
            resendOtp()
        }
    }

    private fun resendOtp() {
        val forgotPasswordViewModel = edu.bluejack24_2.nasigoyeng.data.viewmodel.ForgotPasswordViewModel()

        forgotPasswordViewModel.sendOtpEmail(
            email = email,
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun EditText.setupOtpInput(nextField: EditText?) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) {
                    nextField?.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
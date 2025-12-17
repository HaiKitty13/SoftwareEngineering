package edu.bluejack24_2.nasigoyeng.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.databinding.ActivityRegisterOtpBinding

class RegisterOTP : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterOtpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
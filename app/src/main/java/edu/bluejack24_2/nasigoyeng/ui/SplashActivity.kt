package edu.bluejack24_2.nasigoyeng.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.databinding.ActivitySplashBinding
import edu.bluejack24_2.nasigoyeng.ui.onboarding.OnBoarding1Activity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, OnBoarding1Activity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
package edu.bluejack24_2.nasigoyeng.ui.onboarding

import android.content.Intent
import android.os.Bundle
import edu.bluejack24_2.nasigoyeng.databinding.ActivityOnBoarding1Binding
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity

class OnBoarding1Activity : BaseActivity() {
    private lateinit var binding: ActivityOnBoarding1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoarding1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueButton.setOnClickListener{
            val intent = Intent(this, OnBoarding3Activity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
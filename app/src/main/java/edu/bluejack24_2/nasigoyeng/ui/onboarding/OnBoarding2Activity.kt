package edu.bluejack24_2.nasigoyeng.ui.onboarding

import android.content.Intent
import android.os.Bundle
import edu.bluejack24_2.nasigoyeng.databinding.ActivityOnBoarding2Binding
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity

class OnBoarding2Activity : BaseActivity() {
    private lateinit var binding : ActivityOnBoarding2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoarding2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueButton.setOnClickListener {
            val intent = Intent(this, OnBoarding3Activity::class.java)
            startActivity(intent)
            finish()
        }
        binding.backBtn.setOnClickListener {
            val intent = Intent(this, OnBoarding1Activity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
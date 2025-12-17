package edu.bluejack24_2.nasigoyeng.ui.onboarding

import android.content.Intent
import android.os.Bundle
import edu.bluejack24_2.nasigoyeng.databinding.ActivityOnBoarding3Binding
import edu.bluejack24_2.nasigoyeng.ui.LoginActivity
import edu.bluejack24_2.nasigoyeng.ui.PreferenceActivity
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity

class OnBoarding3Activity : BaseActivity() {
    private lateinit var binding: ActivityOnBoarding3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoarding3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.registerBtn.setOnClickListener {
            val intent = Intent(this, PreferenceActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
package edu.bluejack24_2.nasigoyeng.ui.setting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.databinding.ActivityLanguageSettingBinding
import java.util.Locale
import android.os.Build

class LanguageSettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSettingBinding
    private lateinit var preferences: SharedPreferences

    private val PREF_NAME = "AppPreferences"
    private val KEY_LANGUAGE = "selected_language"
    private var selectedLanguage = "English"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLanguageSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPreferences()
        loadSavedLanguage()
        setupClickListeners()
    }

    override fun attachBaseContext(newBase: Context?) {
        val preferences = newBase?.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val langCode = when (preferences?.getString(KEY_LANGUAGE, "English")) {
            "Indonesia" -> "in"
            else -> "en"
        }

        val context = newBase?.let { setLocale(it, langCode) }
        super.attachBaseContext(context)
    }

    private fun initPreferences() {
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    }

    private fun loadSavedLanguage() {
        selectedLanguage = preferences.getString(KEY_LANGUAGE, "English") ?: "English"

        when (selectedLanguage) {
            "English" -> {
                binding.radioEnglish.isChecked = true
                binding.radioIndonesia.isChecked = false
            }
            "Indonesia" -> {
                binding.radioEnglish.isChecked = false
                binding.radioIndonesia.isChecked = true
            }
        }
    }

    private fun setupClickListeners() {
        // Back button - hapus duplikasi
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Save button
        binding.btnSave.setOnClickListener {
            saveLanguage()
        }

        // English option
        binding.layoutEnglish.setOnClickListener {
            selectLanguage("English")
        }

        binding.radioEnglish.setOnClickListener {
            selectLanguage("English")
        }

        // Indonesia option
        binding.layoutIndonesia.setOnClickListener {
            selectLanguage("Indonesia")
        }

        binding.radioIndonesia.setOnClickListener {
            selectLanguage("Indonesia")
        }
    }

    private fun selectLanguage(language: String) {
        selectedLanguage = language

        when (language) {
            "English" -> {
                binding.radioEnglish.isChecked = true
                binding.radioIndonesia.isChecked = false
            }
            "Indonesia" -> {
                binding.radioEnglish.isChecked = false
                binding.radioIndonesia.isChecked = true
            }
        }
    }

    private fun saveLanguage() {
        preferences.edit()
            .putString(KEY_LANGUAGE, selectedLanguage)
            .apply()

        Toast.makeText(
            this,
            "Language saved: $selectedLanguage",
            Toast.LENGTH_SHORT
        ).show()

        val langCode = when (selectedLanguage) {
            "Indonesia" -> "in"
            else -> "en"
        }
        setLocale(this, langCode)

        recreate()
        sendBroadcast(Intent("edu.bluejack24_2.nasigoyeng.LANGUAGE_CHANGED"))
//        restartApp()
    }


    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
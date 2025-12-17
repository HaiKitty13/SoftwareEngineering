package edu.bluejack24_2.nasigoyeng.ui.setting

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.data.viewmodel.SettingsViewModel
import edu.bluejack24_2.nasigoyeng.databinding.ActivitySettingsBinding
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity
import com.google.firebase.auth.FirebaseAuth


class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupObservers()
        setupClickListeners(prefs)
    }


    private fun setupObservers(){
        viewModel.isDarkTheme.observe(this, Observer { isDarkTheme ->
            if (isDarkTheme) {
                setTheme(R.style.Theme_NaSiGoYeng)
            } else {
                setTheme(R.style.Base_Theme_NaSiGoYeng)
            }
        })

        viewModel.privacyUpdateResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Privacy setting updated", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, it.message ?: "Failed to update privacy", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupClickListeners(prefs: SharedPreferences) {

        binding.privateSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d("SettingsActivity", "Private mode changed: $isChecked")
            viewModel.onPrivacyChanged(isChecked)

            if (isChecked) {
                binding.tvPrivacy.text = getString(R.string.private_mode_on)
            } else {
                binding.tvPrivacy.text = getString(R.string.private_mode_off)
            }
        }


        binding.languageItem.setOnClickListener {
            val intent = Intent(this, LanguageSettingActivity::class.java)
            startActivity(intent)
        }

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = prefs.edit()
            editor.putBoolean("dark_mode", isChecked)
            editor.apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

        }

        binding.logoutItem.setOnClickListener {
            viewModel.onLogoutClicked()
            showLogoutConfirmation()
        }

        binding.tvDeleteAccount.setOnClickListener {
            viewModel.onDeleteAccountClicked()
            showDeleteAccountConfirmation()
        }

        binding.btnBack.setOnClickListener{
            finish()
        }

    }

    private fun restartApp() {
        FirebaseAuth.getInstance().signOut()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun showLogoutConfirmation() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog_log_out, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnLogout = dialogView.findViewById<Button>(R.id.btnDelete)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnLogout.setOnClickListener {
            restartApp()
        }

        dialog.show()
    }

    private fun showDeleteAccountConfirmation() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog_delete, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            viewModel.deleteAccount()
            restartApp()
        }

        dialog.show()
    }

}
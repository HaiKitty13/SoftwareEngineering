package edu.bluejack24_2.nasigoyeng.ui.base

import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.bluejack24_2.nasigoyeng.util.LocaleHelper

open class BaseActivity : AppCompatActivity() {

    private var lastKnownLanguage: String? = null

    private val languageChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            lastKnownLanguage = getCurrentLanguage()
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(
            languageChangedReceiver,
            IntentFilter("edu.bluejack24_2.nasigoyeng.LANGUAGE_CHANGED"),
            Context.RECEIVER_NOT_EXPORTED
        )


        lastKnownLanguage = getCurrentLanguage()
    }

    override fun onResume() {
        super.onResume()
        val currentLang = getCurrentLanguage()
        if (currentLang != lastKnownLanguage) {
            lastKnownLanguage = currentLang
            recreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(languageChangedReceiver)
    }

    override fun attachBaseContext(newBase: Context) {
        val preferences = newBase.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val langCode = when (preferences.getString("selected_language", "English")) {
            "Indonesia" -> "in"
            else -> "en"
        }
        val localizedContext = LocaleHelper.setLocale(newBase, langCode)
        super.attachBaseContext(localizedContext)
    }

    private fun getCurrentLanguage(): String {
        val preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return preferences.getString("selected_language", "English") ?: "English"
    }
}

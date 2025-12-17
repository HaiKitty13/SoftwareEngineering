package edu.bluejack24_2.nasigoyeng

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class MyApplication : Application() {

    companion object {
        private const val PREF_NAME = "AppPreferences"
        private const val KEY_LANGUAGE = "selected_language"
    }

    override fun attachBaseContext(base: Context?) {
        val preferences = base?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val langCode = when (preferences?.getString(KEY_LANGUAGE, "English")) {
            "Indonesia" -> "in"
            else -> "en"
        }

        val context = base?.let { setLocale(it, langCode) }
        super.attachBaseContext(context)
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
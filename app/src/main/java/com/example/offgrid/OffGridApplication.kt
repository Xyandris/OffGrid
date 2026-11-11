package com.example.offgrid

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
/**
 * @brief Zarządza jasnym i ciemnym motywem aplikacji OffGrid.
 *
 * Zapisuje wybór użytkownika w SharedPreferences i przywraca go przy uruchomieniu aplikacji.
 * @note Przed pierwszym wyborem OffGrid korzysta z motywu telefonu.
 */
class OffGridApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val settings = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (settings.contains(DARK_MODE)) {
            applyTheme(this, settings.getBoolean(DARK_MODE, false))
        }
    }

    companion object {
        private const val PREFS = "offgrid_settings"
        private const val DARK_MODE = "dark_mode"

        fun saveTheme(context: Context, dark: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(DARK_MODE, dark)
                .apply()

            applyTheme(context, dark)
        }

        private fun applyTheme(context: Context, dark: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(UiModeManager::class.java)
                if (manager != null) {
                    val mode = if (dark) UiModeManager.MODE_NIGHT_YES
                    else UiModeManager.MODE_NIGHT_NO
                    manager.setApplicationNightMode(mode)
                    return
                }
            }

            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}
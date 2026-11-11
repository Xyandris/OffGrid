package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivitySettingsBinding

/**
 * @brief Ekran ustawień z wejściem do deweloperskich formularzy konta.
 * Obsługuje zakładki, powrót oraz otwieranie logowania i rejestracji.
 * @note Pozostałe pozycje ustawień i wylogowanie są na razie elementami makiety.
 */
class SettingsActivity : AppCompatActivity() {
    /**
     * @brief Inicjalizuje nawigację ustawień i przyciski otwierające formularze konta.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.settingsScreen.bottomNavigation, MainTab.SETTINGS)

        binding.settingsScreen.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.settingsScreen.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.settingsScreen.settingsBack.setOnClickListener { finish() }
    }
}

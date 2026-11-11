package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityLoginBinding

/**
 * @brief Ekran formularza logowania otwierany z sekcji deweloperskiej ustawień.
 *
 * Przejście do rejestracji kończy tę aktywność, aby powrót nie odtwarzał kolejnych formularzy na stosie.
 * @note Logowanie i reset hasła wyświetlają wyłącznie komunikaty o braku konfiguracji.
 */
class LoginActivity : AppCompatActivity() {
    /**
     * @brief Podłącza powrót, przejście do rejestracji oraz komunikaty zastępujące uwierzytelnianie.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)

        binding.loginScreen.loginOpenRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            // Zamknij logowanie, aby z rejestracji można było wrócić bezpośrednio do ustawień.
            finish()
        }
        binding.loginScreen.loginBack.setOnClickListener { finish() }
        binding.loginScreen.loginSubmit.setOnClickListener {
            // Tutaj podłącz usługę uwierzytelniania.
            Toast.makeText(this, R.string.login_not_configured, Toast.LENGTH_SHORT).show()
        }
        binding.loginScreen.loginForgotPassword.setOnClickListener {
            Toast.makeText(this, R.string.password_reset_not_configured, Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityRegisterBinding

/**
 * @brief Ekran formularza rejestracji dostępny z ustawień lub formularza logowania.
 * Przejście do logowania kończy bieżącą aktywność, zachowując możliwość powrotu bezpośrednio do ustawień.
 * @note Przycisk rejestracji wyświetla komunikat.
 */
class RegisterActivity : AppCompatActivity() {
    /**
     * @brief Podłącza przejście do logowania, zamknięcie formularza i komunikat rejestracji.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)

        binding.registerScreen.registerOpenLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.registerScreen.registerBack.setOnClickListener { finish() }
        binding.registerScreen.registerSubmit.setOnClickListener {
            // Tutaj podłącz usługę uwierzytelniania, aby tworzyć konta.
            Toast.makeText(this, R.string.register_not_configured, Toast.LENGTH_SHORT).show()
        }
    }
}

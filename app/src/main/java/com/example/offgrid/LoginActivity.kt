/**
 * @file LoginActivity.kt
 * @brief Ekran logowania użytkownika z wykorzystaniem Firebase Authentication.
 */
package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * @brief Ekran formularza logowania otwierany z sekcji deweloperskiej ustawień.
 *
 * Obsługuje walidację danych oraz uwierzytelnianie użytkownika przez Firebase Auth
 * (e-mail i hasło). Po poprawnym zalogowaniu czyści stos aktywności i przenosi
 * użytkownika bezpośrednio do ekranu głównego aplikacji.
 *
 * @note Przejście do rejestracji zamyka bieżącą aktywność (`finish()`), aby przycisk
 *       wstecz prowadził bezpośrednio do ustawień. Opcja resetu hasła wyświetla
 *       obecnie jedynie komunikat o braku konfiguracji.
 * @see RegisterActivity
 * @see MainActivity
 */
class LoginActivity : AppCompatActivity() {

    /**
     * @brief Inicjalizuje widok formularza, nawigację oraz proces logowania.
     *
     * Konfiguruje obsługę przycisków powrotu, przejścia do rejestracji i przypomnienia hasła.
     * Po zatwierdzeniu formularza sprawdza wypełnienie pól, blokuje przycisk na czas komunikacji
     * z Firebase i wykonuje logowanie (`signInWithEmailAndPassword`). Po sukcesie otwiera
     * `MainActivity` z wyczyszczonym stosem aktywności (`FLAG_ACTIVITY_CLEAR_TASK`).
     *
     * @param savedInstanceState Zapisany stan instancji aktywności lub `null` przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)

        binding.loginScreen.loginOpenRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        binding.loginScreen.loginBack.setOnClickListener { finish() }
        binding.loginScreen.loginSubmit.setOnClickListener {
            val form = binding.loginScreen
            val email = form.loginEmail.text.toString().trim()
            val password = form.loginPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            form.loginSubmit.isEnabled = false

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    form.loginSubmit.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Wrong password or email. Tryb again", Toast.LENGTH_LONG).show()
                    }
                }
        }
        binding.loginScreen.loginForgotPassword.setOnClickListener {
            Toast.makeText(this, R.string.password_reset_not_configured, Toast.LENGTH_SHORT).show()
        }
    }
}
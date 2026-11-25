/**
 * @file offgrid/LoginActivity.kt
 * @brief Ekran logowania użytkownika do aplikacji OffGrid.
 * @details Obsługuje walidację danych logowania, uwierzytelnianie przy użyciu Firebase Authentication,
 *  nawigację do rejestracji oraz podgląd ekranu w trybie deweloperskim.
 */
package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.offgrid.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * @brief Aktywność prezentująca formularz logowania za pomocą adresu e-mail i hasła.
 */
class LoginActivity : BaseActivity() {
    /**
     * @brief Wyłącza wymóg posiadania aktywnej sesji użytkownika na ekranie logowania.
     */
    override val requiresLogin = false

    /**
     * @brief Leniwie inicjalizowana instancja `FirebaseAuth` wykorzystywana do logowania.
     */
    private val auth by lazy { FirebaseAuth.getInstance() }

    /**
     * @brief Tworzy interfejs ekranu logowania i podpina obsługę przycisków formularza.
     * @details Konfiguruje widoczność przycisku powrotu w trybie deweloperskim, przejście do
     *          `RegisterActivity`, walidację adresu e-mail i hasła oraz logowanie w Firebase.
     * @param savedInstanceState Zapisany stan instancji aktywności lub `null`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        val form = binding.loginScreen
        val developerScreen = intent.getBooleanExtra(DEVELOPER_SCREEN, false)

        form.loginBack.visibility = if (developerScreen) View.VISIBLE else View.GONE
        form.loginBack.setOnClickListener { finish() }
        form.loginOpenRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java)
                .putExtra(DEVELOPER_SCREEN, developerScreen))
            finish()
        }
        form.loginSubmit.setOnClickListener {
            val email = form.loginEmail.text.toString().trim()
            val password = form.loginPassword.text.toString()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isBlank()) {
                Toast.makeText(this, R.string.auth_invalid_login, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            form.loginSubmit.isEnabled = false
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                form.loginSubmit.isEnabled = true
                if (task.isSuccessful) {
                    AppLock.unlock(this)
                    openChats()
                } else {
                    Toast.makeText(this, R.string.auth_login_failed, Toast.LENGTH_LONG).show()
                }
            }
        }
        form.loginForgotPassword.setOnClickListener {
            Toast.makeText(this, R.string.password_reset_not_configured, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * @brief Przekierowuje zalogowanego użytkownika bezpośrednio do listy rozmów.
     * @details Przekierowanie jest pomijane, gdy aktywność została otwarta z flagą `DEVELOPER_SCREEN`.
     */
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null && !intent.getBooleanExtra(DEVELOPER_SCREEN, false)) {
            openChats()
        }
    }
}

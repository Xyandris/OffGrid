/**
 * @file offgrid/RegisterActivity.kt
 * @brief Ekran rejestracji nowego konta użytkownika w aplikacji OffGrid.
 * @details Realizuje walidację danych formularza, tworzenie konta w Firebase Authentication,
 * zapis nazwy wyświetlanej użytkownika oraz obsługę nawigacji powrotnej.
 */
package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.addCallback
import com.example.offgrid.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * @brief Aktywność obsługująca tworzenie nowego profilu użytkownika.
 */
class RegisterActivity : BaseActivity() {
    /**
     * @brief Wyłącza wymóg posiadania aktywnej sesji użytkownika na ekranie rejestracji.
     */
    override val requiresLogin = false

    /**
     * @brief Leniwie inicjalizowana instancja `FirebaseAuth` wykorzystywana do rejestracji konta.
     */
    private val auth by lazy { FirebaseAuth.getInstance() }

    /**
     * @brief Tworzy widok formularza rejestracji i konfiguruje obsługę interakcji użytkownika.
     * @details Waliduje nazwę użytkownika, format adresu e-mail, długość i zgodność haseł,
     * tworzy konto w Firebase Authentication, zapisuje `displayName` profilu i otwiera czaty.
     * @param savedInstanceState Zapisany stan instancji aktywności lub `null`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        val form = binding.registerScreen
        val developerScreen = intent.getBooleanExtra(DEVELOPER_SCREEN, false)

        form.registerOpenLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)
                .putExtra(DEVELOPER_SCREEN, developerScreen))
            finish()
        }
        form.registerBack.setOnClickListener { goBack(developerScreen) }
        onBackPressedDispatcher.addCallback(this) { goBack(developerScreen) }
        form.registerSubmit.setOnClickListener {
            val name = form.registerName.text.toString().trim()
            val email = form.registerEmail.text.toString().trim()
            val password = form.registerPassword.text.toString()
            val confirmation = form.registerConfirmPassword.text.toString()
            if (name.isBlank()) {
                form.registerName.error = getString(R.string.auth_name_required)
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                password.length < 6 || password != confirmation) {
                Toast.makeText(this, R.string.auth_invalid_registration, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            form.registerSubmit.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    form.registerSubmit.isEnabled = true
                    Toast.makeText(this, R.string.auth_register_failed, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }
                val user = task.result?.user ?: return@addOnCompleteListener
                val profile = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                user.updateProfile(profile).addOnCompleteListener(this) { profileTask ->
                    if (!profileTask.isSuccessful) {
                        Toast.makeText(this, R.string.auth_profile_not_saved, Toast.LENGTH_LONG).show()
                    }
                    AppLock.unlock(this)
                    openChats()
                }
            }
        }
    }

    /**
     * @brief Przekierowuje zalogowanego użytkownika bezpośrednio do ekranu rozmów.
     * @details Przekierowanie jest pomijane, gdy ekran otwarto w trybie deweloperskim (`DEVELOPER_SCREEN`).
     */
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null && !intent.getBooleanExtra(DEVELOPER_SCREEN, false)) {
            openChats()
        }
    }

    /**
     * @brief Obsługuje cofnięcie z ekranu rejestracji za pomocą przycisku w nagłówku lub gestu wstecz.
     * @param developerScreen Wartość `true`, jeśli ekran został otwarty z ustawień deweloperskich.
     */
    private fun goBack(developerScreen: Boolean) {
        if (developerScreen) finish() else openLogin()
    }
}

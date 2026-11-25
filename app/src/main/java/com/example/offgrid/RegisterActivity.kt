/**
 * @file app/src/main/java/com/example/offgrid/RegisterActivity.kt
 * @brief Rejestracja konta i przygotowanie publicznego profilu wyszukiwarki.
 * @details Łączy Firebase Authentication z zapisem dokumentu users o identyfikatorze równym UID konta.
 */

package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import com.example.offgrid.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * @brief Obsługuje formularz zakładania konta w OffGrid.
 * @details Sprawdza dane, tworzy konto i zapisuje nazwę oczekującą na synchronizację.
 *          Następnie aktualizuje profil Authentication i uruchamia zapis profilu Firestore.
 *          Dokończenie operacji w tle nie wymusza przejścia do nieaktywnego ekranu.
 */
class RegisterActivity : BaseActivity() {
    /**
     * @brief Udostępnia formularz rejestracji także osobom niezalogowanym.
     */
    override val requiresLogin = false
    /**
     * @brief Instancja Firebase Authentication tworząca konto i udostępniająca bieżącą sesję.
     */
    private val auth by lazy { FirebaseAuth.getInstance() }
    /**
     * @brief Chroni trwającą rejestrację przed automatycznym przejściem wykonywanym w onStart.
     */
    private var registering = false

    /**
     * @brief Buduje formularz oraz obsługę walidacji, rejestracji i zapisu profilu.
     * @param savedInstanceState Stan poprzedniej instancji aktywności albo null.
     * @details Nazwa musi być poprawna dla UserSearchText; hasło ma minimum sześć znaków
     *          i musi zgadzać się z potwierdzeniem. Błąd synchronizacji profilu pozwala ponowić ją później.
     * @see UserProfiles.ensureCurrentProfile
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
            val name = UserSearchText.displayName(form.registerName.text.toString())
            val email = form.registerEmail.text.toString().trim()
            val password = form.registerPassword.text.toString()
            val confirmation = form.registerConfirmPassword.text.toString()
            if (!UserSearchText.isValidName(name)) {
                form.registerName.error = getString(R.string.search_name_invalid)
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                password.length < 6 || password != confirmation) {
                Toast.makeText(this, R.string.auth_invalid_registration, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            form.registerSubmit.isEnabled = false
            registering = true
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    registering = false
                    if (!isFinishing && !isDestroyed) {
                        form.registerSubmit.isEnabled = true
                        Toast.makeText(this, R.string.auth_register_failed, Toast.LENGTH_LONG).show()
                    }
                    return@addOnCompleteListener
                }
                val user = task.result?.user
                if (user == null || auth.currentUser?.uid != user.uid) {
                    registering = false
                    form.registerSubmit.isEnabled = true
                    return@addOnCompleteListener
                }
                UserProfiles.rememberDisplayName(user.uid, name)
                val profile = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                user.updateProfile(profile).addOnCompleteListener profileComplete@ { profileTask ->
                    if (auth.currentUser?.uid != user.uid) {
                        registering = false
                        return@profileComplete
                    }
                    if (!profileTask.isSuccessful && !isFinishing && !isDestroyed) {
                        Toast.makeText(this, R.string.auth_profile_not_saved, Toast.LENGTH_LONG).show()
                    }
                    UserProfiles.ensureCurrentProfile(name).addOnCompleteListener { syncTask ->
                        registering = false
                        if (!isFinishing && !isDestroyed && auth.currentUser?.uid == user.uid
                            && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            if (!syncTask.isSuccessful) {
                                Toast.makeText(this, R.string.search_registration_profile_pending,
                                    Toast.LENGTH_LONG).show()
                            }
                            AppLock.unlock(this)
                            openChats()
                        }
                    }
                }
            }
        }
    }

    /**
     * @brief Otwiera rozmowy, jeżeli użytkownik jest już zalogowany i nie trwa rejestracja.
     * @details Automatyczne przejście jest pomijane dla formularza otwartego w trybie deweloperskim.
     */
    override fun onStart() {
        super.onStart()
        if (!registering && auth.currentUser != null
            && !intent.getBooleanExtra(DEVELOPER_SCREEN, false)) {
            openChats()
        }
    }

    /**
     * @brief Obsługuje powrót z formularza zgodnie z miejscem jego otwarcia.
     * @param developerScreen Wartość true zamyka ekran deweloperski; false otwiera logowanie.
     */
    private fun goBack(developerScreen: Boolean) {
        if (developerScreen) finish() else openLogin()
    }
}

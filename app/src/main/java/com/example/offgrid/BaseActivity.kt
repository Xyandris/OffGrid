/**
 * @file offgrid/BaseActivity.kt
 * @brief Bazowa aktywność aplikacji OffGrid egzekwująca logowanie, blokadę ekranu i ochronę zawartości.
 * @details Nadzoruje obecność sesji Firebase, ustawia flagę `FLAG_SECURE` dla zablokowanych kont
 * oraz przekierowuje użytkownika do `LoginActivity` lub `LockActivity` w razie potrzeby.
 */
package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * @brief Bazowa klasa dla aktywności wymagających wspólnej obsługi sesji i zabezpieczeń okna.
 */
open class BaseActivity : AppCompatActivity() {
    /**
     * @brief Określa, czy aktywność wymaga aktywnej sesji zalogowanego użytkownika.
     */
    protected open val requiresLogin = true

    /**
     * @brief Inicjalizuje aktywność i przekierowuje niezalogowanego użytkownika do ekranu logowania.
     * @param savedInstanceState Zapisany stan instancji aktywności lub `null`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (requiresLogin && FirebaseAuth.getInstance().currentUser == null) openLogin()
    }

    /**
     * @brief Weryfikuje uwierzytelnienie użytkownika, blokadę aplikacji oraz widoczność interfejsu po wznowieniu.
     * @details Ustawia lub usuwa flagę `FLAG_SECURE`, ukrywa widok `android.R.id.content` podczas
     * przekierowania do `LoginActivity` lub `LockActivity`, a po odblokowaniu przywraca jego widoczność.
     */
    override fun onResume() {
        super.onResume()
        if (isFinishing) return
        val content = findViewById<View>(android.R.id.content)
        if (requiresLogin && FirebaseAuth.getInstance().currentUser == null) {
            content.visibility = View.INVISIBLE
            openLogin()
            return
        }
        if (AppLock.isEnabled(this)) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

        if (AppLock.isLocked(this)) {
            content.visibility = View.INVISIBLE
            startActivity(Intent(this, LockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        } else {
            content.visibility = View.VISIBLE
        }
    }

    /**
     * @brief Zabezpiecza podgląd aktywności w przełączniku zadań przy włączonej blokadzie aplikacji.
     * @details Dodaje flagę `FLAG_SECURE` i ukrywa główny widok zawartości przed wstrzymaniem aktywności.
     */
    override fun onPause() {
        if (AppLock.isEnabled(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            findViewById<View>(android.R.id.content).visibility = View.INVISIBLE
        }
        super.onPause()
    }
}

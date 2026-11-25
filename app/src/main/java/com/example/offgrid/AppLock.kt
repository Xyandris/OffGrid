/**
 * @file offgrid/AppLock.kt
 * @brief Zarządzanie stanem blokady biometrycznej i zabezpieczenia urządzenia w aplikacji OffGrid.
 * @details Przechowuje ustawienie włączenia blokady w `SharedPreferences` osobno dla każdego
 *          identyfikatora UID użytkownika Firebase oraz śledzi stan odblokowania bieżącej sesji.
 */
package com.example.offgrid

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

/**
 * @brief Singleton kontrolujący konfigurację oraz bieżący stan blokady aplikacji.
 */
internal object AppLock {
    /**
     * @brief Identyfikator UID użytkownika, dla którego bieżąca sesja została odblokowana.
     */
    private var unlockedUserId: String? = null

    /**
     * @brief Zwraca identyfikator UID aktualnie zalogowanego użytkownika Firebase.
     * @return Identyfikator UID użytkownika lub `null`, gdy nikt nie jest zalogowany.
     */
    private fun userId() = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * @brief Sprawdza, czy blokada aplikacji jest włączona dla bieżącego użytkownika.
     * @param context Kontekst używany do odczytu pliku preferencji `offgrid_biometrics`.
     * @return Wartość `true`, jeśli blokada jest aktywna dla zalogowanego konta; w przeciwnym razie `false`.
     */
    fun isEnabled(context: Context): Boolean {
        val uid = userId() ?: return false
        return context.getSharedPreferences("offgrid_biometrics", Context.MODE_PRIVATE)
            .getBoolean(uid, false)
    }

    /**
     * @brief Włącza lub wyłącza blokadę aplikacji dla bieżącego użytkownika.
     * @details Zapisuje flagę w `SharedPreferences` pod kluczem równym UID użytkownika, a następnie
     * odblokowuje bieżącą sesję (przy włączeniu) lub resetuje stan odblokowania (przy wyłączeniu).
     * @param context Kontekst aplikacji lub aktywności.
     * @param enabled Docelowy stan blokady aplikacji.
     */
    fun setEnabled(context: Context, enabled: Boolean) {
        val uid = userId() ?: return
        context.getSharedPreferences("offgrid_biometrics", Context.MODE_PRIVATE)
            .edit().putBoolean(uid, enabled).apply()
        if (enabled) unlock(context) else lock()
    }

    /**
     * @brief Określa, czy dostęp do treści aplikacji wymaga obecnie uwierzytelnienia.
     * @param context Kontekst używany do sprawdzenia konfiguracji blokady.
     * @return Wartość `true`, gdy blokada jest włączona, a sesja bieżącego użytkownika nie została odblokowana.
     */
    fun isLocked(context: Context): Boolean =
        isEnabled(context) && unlockedUserId != userId()

    /**
     * @brief Oznacza sesję bieżącego użytkownika jako odblokowaną, jeśli aplikacja jest na pierwszym planie.
     * @param context Kontekst umożliwiający weryfikację stanu `OffGridApplication.isInForeground`.
     */
    fun unlock(context: Context) {
        if ((context.applicationContext as OffGridApplication).isInForeground) {
            unlockedUserId = userId()
        }
    }

    /**
     * @brief Blokuje bieżącą sesję aplikacji poprzez wyczyszczenie identyfikatora odblokowanego użytkownika.
     */
    fun lock() {
        unlockedUserId = null
    }
}

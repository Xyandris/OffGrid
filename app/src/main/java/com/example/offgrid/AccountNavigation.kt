/**
 * @file offgrid/AccountNavigation.kt
 * @brief Funkcje rozszerzające nawigację między ekranami konta a głównym widokiem czatów.
 * @details Definiuje stałą identyfikującą tryb podglądu deweloperskiego oraz funkcje pomocnicze
 *otwierające `MainActivity` lub `LoginActivity` z wyczyszczeniem stosu aktywności.
 */
package com.example.offgrid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

/**
 * @brief Klucz dodatku `Intent` oznaczający uruchomienie ekranu uwierzytelniania z ustawień deweloperskich.
 */
internal const val DEVELOPER_SCREEN = "developer_screen"

/**
 * @brief Przechodzi do głównego ekranu rozmów i czyści stos poprzednich aktywności.
 * @details Uruchamia `MainActivity` z flagami `FLAG_ACTIVITY_NEW_TASK` oraz
 *          `FLAG_ACTIVITY_CLEAR_TASK`, a następnie kończy bieżącą aktywność.
 */
internal fun AppCompatActivity.openChats() {
    startActivity(Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
    finish()
}

/**
 * @brief Przechodzi do ekranu logowania i czyści stos poprzednich aktywności.
 * @details Uruchamia `LoginActivity` z flagami `FLAG_ACTIVITY_NEW_TASK` oraz
 * `FLAG_ACTIVITY_CLEAR_TASK`, a następnie kończy bieżącą aktywność.
 */
internal fun AppCompatActivity.openLogin() {
    startActivity(Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
    finish()
}

package com.example.offgrid

import android.graphics.Color
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * @brief Ustawia widok aktywności i wspólną obsługę przestrzeni przy krawędziach ekranu.
 *
 * Włącza rysowanie pod paskami systemowymi, wybiera styl pasków dla jasnego tła i rejestruje dopasowanie odstępów do pasków oraz klawiatury ekranowej.
 *
 * @param root Główny widok layoutu utworzony przez View Binding.
 * @see applyScreenInsets
 */
internal fun AppCompatActivity.showOffGridScreen(root: View) {
    // Te układy mają jasne tło zarówno w jasnym, jak i ciemnym trybie systemu.
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    )
    setContentView(root)
    applyScreenInsets(root)
}

/**
 * @brief Dodaje do pierwotnego paddingu widoku odstępy pasków systemowych i klawiatury.
 *
 * Zapamiętuje początkowy padding, dzięki czemu kolejne zdarzenia insets nie sumują
 * wcześniej dodanych odstępów. Listener zwraca otrzymany WindowInsetsCompat, pozostawiając możliwość dalszego przekazania insets do widoków potomnych.
 *
 * @param root Widok, na którym zostanie zarejestrowany listener zmieniający padding.
 * @note Wywołuj przy konfiguracji nowego widoku, przed pierwszym zastosowaniem insets;
 * ponowna rejestracja po zmianie paddingu przyjęłaby powiększone wartości jako bazę.
 */
internal fun applyScreenInsets(root: View) {
    val left = root.paddingLeft
    val top = root.paddingTop
    val right = root.paddingRight
    val bottom = root.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        view.setPadding(
            left + insets.left, top + insets.top,
            right + insets.right, bottom + insets.bottom
        )
        windowInsets
    }
}

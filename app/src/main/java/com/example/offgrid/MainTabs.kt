package com.example.offgrid

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.BottomNavigationBinding

/**
 * @brief Główne zakładki aplikacji wraz z aktywnościami docelowymi.
 * @param activityClass Klasa aktywności uruchamianej po wybraniu zakładki.
 */
internal enum class MainTab(val activityClass: Class<out AppCompatActivity>) {
    CHATS(MainActivity::class.java),
    SEARCH(SearchActivity::class.java),
    REQUESTS(RequestsActivity::class.java),
    SETTINGS(SettingsActivity::class.java)
}

/**
 * @brief Podłącza wspólny pasek zakładek i wyróżnia aktualnie otwarty ekran.
 *
 * Wybranie bieżącej zakładki nie uruchamia nowej aktywności. Pozostałe przejścia
 * korzystają z FLAG_ACTIVITY_CLEAR_TOP i FLAG_ACTIVITY_SINGLE_TOP. Przy opuszczaniu
 * zakładki innej niż czaty bieżąca aktywność jest kończona, aby nie gromadzić
 * kolejnych odwiedzin zakładek na stosie.
 *
 * @param navigation Powiązanie widoków wspólnego layoutu bottom_navigation.xml.
 * @param currentTab Zakładka odpowiadająca aktywności wywołującej tę funkcję.
 * @pre Standardowy przepływ nawigacji rozpoczyna się w MainActivity, która pozostaje
 * na stosie jako ekran powrotu z pozostałych zakładek.
 */
internal fun AppCompatActivity.bindMainTabs(
    navigation: BottomNavigationBinding,
    currentTab: MainTab
) {
    val tabs = mapOf(
        MainTab.CHATS to navigation.tabChats,
        MainTab.SEARCH to navigation.tabSearch,
        MainTab.REQUESTS to navigation.tabRequests,
        MainTab.SETTINGS to navigation.tabSettings
    )
    tabs.forEach { (tab, button) ->
        button.isSelected = tab == currentTab
        button.setTypeface(
            Typeface.SANS_SERIF,
            if (button.isSelected) Typeface.BOLD else Typeface.NORMAL
        )
        button.setOnClickListener {
            if (tab != currentTab) {
                startActivity(
                    Intent(this, tab.activityClass).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                )
                // Zachowaj czaty na stosie jako ekran powrotu z pozostałych zakładek.
                if (currentTab != MainTab.CHATS) finish()
            }
        }
    }
}

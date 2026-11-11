package com.example.offgrid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivitySearchBinding

/**
 * @brief Ekran makiety wyszukiwania kontaktów z działającą nawigacją zakładek.
 * @note Pole zapytania i przykładowe wyniki nie realizują jeszcze wyszukiwania.
 */
class SearchActivity : AppCompatActivity() {
    /**
     * @brief Wczytuje ekran wyszukiwania i oznacza jego zakładkę jako aktywną.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySearchBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.searchScreen.bottomNavigation, MainTab.SEARCH)
    }
}

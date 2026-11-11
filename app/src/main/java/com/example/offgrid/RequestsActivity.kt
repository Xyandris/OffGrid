package com.example.offgrid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityRequestsBinding

/**
 * @brief Ekran przykładowych zaproszeń do kontaktów z nawigacją i przyciskiem powrotu.
 * @note Akceptowanie i odrzucanie zaproszeń nie są jeszcze obsługiwane.
 */
class RequestsActivity : AppCompatActivity() {
    /**
     * @brief Wczytuje zaproszenia, zaznacza ich zakładkę i podłącza zamknięcie aktywności.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRequestsBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.requestsScreen.bottomNavigation, MainTab.REQUESTS)
        binding.requestsScreen.requestsBack.setOnClickListener { finish() }
    }
}

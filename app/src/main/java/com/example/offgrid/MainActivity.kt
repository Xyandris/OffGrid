package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityMainBinding

/**
 * @brief Główny ekran rozmów i punkt startowy aplikacji OffGrid.
 * Udostępnia cztery zakładki oraz otwieranie rozmów z przykładowymi kontaktami.
 * Pozostaje pod pozostałymi zakładkami na stosie aktywności.
 */
class MainActivity : AppCompatActivity() {
    /**
     * @brief Tworzy widok listy rozmów, zaznacza zakładkę czatów i podłącza wybór kontaktu.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.chatList.bottomNavigation, MainTab.CHATS)
        binding.chatList.contactsSarah.setOnClickListener { openChat("Sarah Miller") }
        binding.chatList.contactsAlex.setOnClickListener { openChat("Alex Johnson") }
    }

    /**
     * @brief Otwiera ekran rozmowy z nazwą wybranego kontaktu w danych Intent.
     * @param name Nazwa wyświetlana w nagłówku rozmowy i używana do utworzenia inicjałów.
     * @see ChatActivity.CONTACT_NAME
     */
    private fun openChat(name: String) {
        startActivity(Intent(this, ChatActivity::class.java).putExtra(ChatActivity.CONTACT_NAME, name))
    }
}

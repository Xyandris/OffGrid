package com.example.offgrid

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ScreenChatBinding

/**
 * @brief Wyświetla rozmowy z kontaktem wskazanym przez Intent.
 * @note Wysyłanie wiadomości i dodawanie załączników nie są jeszcze zaimplementowane.
 */
class ChatActivity : AppCompatActivity() {
    /**
     * @brief Ustawia nazwę, inicjały, widoczność przykładowych wiadomości i przycisk powrotu.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ScreenChatBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        val name = intent.getStringExtra(CONTACT_NAME) ?: "Sarah Miller"
        binding.chatContactName.text = name
        binding.chatAvatar.text = name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("")
        binding.chatBack.setOnClickListener { finish() }
        if (name != "Sarah Miller") {
            binding.chatMessage1.visibility = View.GONE
            binding.chatMessage2.visibility = View.GONE
            binding.chatMessage3.visibility = View.GONE
        }
    }

    companion object {
        /**
         * @brief Klucz tekstowej wartości Intent zawierającej nazwę rozmówcy.
         * @note Brak wartości powoduje użycie nazwy Sarah Miller w onCreate.
         */
        const val CONTACT_NAME = "com.example.offgrid.CONTACT_NAME"
    }
}

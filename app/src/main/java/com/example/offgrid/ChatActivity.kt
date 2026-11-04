package com.example.offgrid

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ScreenChatBinding

class ChatActivity : AppCompatActivity() {
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
        const val CONTACT_NAME = "com.example.offgrid.CONTACT_NAME"
    }
}

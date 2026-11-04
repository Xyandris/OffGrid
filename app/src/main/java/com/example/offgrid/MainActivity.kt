package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.chatList.bottomNavigation, MainTab.CHATS)
        binding.chatList.contactsSarah.setOnClickListener { openChat("Sarah Miller") }
        binding.chatList.contactsAlex.setOnClickListener { openChat("Alex Johnson") }
    }

    private fun openChat(name: String) {
        startActivity(Intent(this, ChatActivity::class.java).putExtra(ChatActivity.CONTACT_NAME, name))
    }
}

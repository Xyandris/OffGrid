package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.settingsScreen.bottomNavigation, MainTab.SETTINGS)

        binding.settingsScreen.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.settingsScreen.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.settingsScreen.settingsBack.setOnClickListener { finish() }
    }
}

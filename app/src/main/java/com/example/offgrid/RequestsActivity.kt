package com.example.offgrid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityRequestsBinding

class RequestsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRequestsBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.requestsScreen.bottomNavigation, MainTab.REQUESTS)
        binding.requestsScreen.requestsBack.setOnClickListener { finish() }
    }
}

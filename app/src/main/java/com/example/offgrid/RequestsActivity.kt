package com.example.offgrid

import android.os.Bundle
import com.example.offgrid.databinding.ActivityRequestsBinding

class RequestsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityRequestsBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.requestsScreen.bottomNavigation, MainTab.REQUESTS)
        binding.requestsScreen.requestsBack.setOnClickListener { finish() }
    }
}

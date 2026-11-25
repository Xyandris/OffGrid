package com.example.offgrid

import android.os.Bundle
import com.example.offgrid.databinding.ActivitySearchBinding

class SearchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivitySearchBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.searchScreen.bottomNavigation, MainTab.SEARCH)
    }
}

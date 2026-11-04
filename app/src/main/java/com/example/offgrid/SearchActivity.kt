package com.example.offgrid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySearchBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.searchScreen.bottomNavigation, MainTab.SEARCH)
    }
}

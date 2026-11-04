package com.example.offgrid

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.BottomNavigationBinding

internal enum class MainTab(val activityClass: Class<out AppCompatActivity>) {
    CHATS(MainActivity::class.java),
    SEARCH(SearchActivity::class.java),
    REQUESTS(RequestsActivity::class.java),
    SETTINGS(SettingsActivity::class.java)
}

internal fun AppCompatActivity.bindMainTabs(
    navigation: BottomNavigationBinding,
    currentTab: MainTab
) {
    val tabs = mapOf(
        MainTab.CHATS to navigation.tabChats,
        MainTab.SEARCH to navigation.tabSearch,
        MainTab.REQUESTS to navigation.tabRequests,
        MainTab.SETTINGS to navigation.tabSettings
    )
    tabs.forEach { (tab, button) ->
        button.isSelected = tab == currentTab
        button.setTypeface(
            Typeface.SANS_SERIF,
            if (button.isSelected) Typeface.BOLD else Typeface.NORMAL
        )
        button.setOnClickListener {
            if (tab != currentTab) {
                startActivity(
                    Intent(this, tab.activityClass).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                )
                // Keep Chats underneath every tab instead of stacking tab visits.
                if (currentTab != MainTab.CHATS) finish()
            }
        }
    }
}

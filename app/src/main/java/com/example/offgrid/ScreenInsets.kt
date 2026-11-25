package com.example.offgrid

import android.graphics.Color
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

internal fun AppCompatActivity.showOffGridScreen(root: View) {
    enableEdgeToEdge()
    setContentView(root)
    applyScreenInsets(root)
}

internal fun applyScreenInsets(root: View) {
    val left = root.paddingLeft
    val top = root.paddingTop
    val right = root.paddingRight
    val bottom = root.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        view.setPadding(
            left + insets.left, top + insets.top,
            right + insets.right, bottom + insets.bottom
        )
        windowInsets
    }
}

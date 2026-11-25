package com.example.offgrid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

internal const val DEVELOPER_SCREEN = "developer_screen"

internal fun AppCompatActivity.openChats() {
    startActivity(Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
    finish()
}

internal fun AppCompatActivity.openLogin() {
    startActivity(Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
    finish()
}

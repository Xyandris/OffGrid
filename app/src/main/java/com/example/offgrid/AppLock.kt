package com.example.offgrid

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

internal object AppLock {
    private var unlockedUserId: String? = null

    private fun userId() = FirebaseAuth.getInstance().currentUser?.uid

    fun isEnabled(context: Context): Boolean {
        val uid = userId() ?: return false
        return context.getSharedPreferences("offgrid_biometrics", Context.MODE_PRIVATE)
            .getBoolean(uid, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val uid = userId() ?: return
        context.getSharedPreferences("offgrid_biometrics", Context.MODE_PRIVATE)
            .edit().putBoolean(uid, enabled).apply()
        if (enabled) unlock(context) else lock()
    }

    fun isLocked(context: Context): Boolean =
        isEnabled(context) && unlockedUserId != userId()

    fun unlock(context: Context) {
        if ((context.applicationContext as OffGridApplication).isInForeground) {
            unlockedUserId = userId()
        }
    }

    fun lock() {
        unlockedUserId = null
    }
}

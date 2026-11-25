package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    protected open val requiresLogin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (requiresLogin && FirebaseAuth.getInstance().currentUser == null) openLogin()
    }

    override fun onResume() {
        super.onResume()
        if (isFinishing) return
        val content = findViewById<View>(android.R.id.content)
        if (requiresLogin && FirebaseAuth.getInstance().currentUser == null) {
            content.visibility = View.INVISIBLE
            openLogin()
            return
        }
        if (AppLock.isEnabled(this)) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

        if (AppLock.isLocked(this)) {
            content.visibility = View.INVISIBLE
            startActivity(Intent(this, LockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        } else {
            content.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        if (AppLock.isEnabled(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            findViewById<View>(android.R.id.content).visibility = View.INVISIBLE
        }
        super.onPause()
    }
}

package com.example.offgrid

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.example.offgrid.databinding.ActivityLockBinding
import com.google.firebase.auth.FirebaseAuth

class LockActivity : AppCompatActivity() {
    private lateinit var authentication: DeviceAuthentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser == null) {
            openLogin()
            return
        }
        if (!AppLock.isLocked(this)) {
            finish()
            return
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        val binding = ActivityLockBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        authentication = DeviceAuthentication(this,
            onSuccess = {
                AppLock.unlock(this)
                if (!AppLock.isLocked(this)) finish()
            },
            onMessage = { binding.lockStatus.text = it }
        )
        binding.lockRetry.setOnClickListener { authentication.authenticate() }
        onBackPressedDispatcher.addCallback(this) { moveTaskToBack(true) }
    }

    override fun onResume() {
        super.onResume()
        if (isFinishing) return
        if (FirebaseAuth.getInstance().currentUser == null) {
            openLogin()
        } else if (!AppLock.isLocked(this)) {
            finish()
        } else if (::authentication.isInitialized && !authentication.hasAttempted) {
            window.decorView.post {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
                    !isFinishing && !authentication.hasAttempted) {
                    authentication.authenticate()
                }
            }
        }
    }

    override fun onStop() {
        if (!isChangingConfigurations && ::authentication.isInitialized) {
            authentication.prepareForNextOpen()
        }
        super.onStop()
    }
}

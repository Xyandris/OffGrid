package com.example.offgrid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityBiometricsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BiometricsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBiometricsBinding
    private lateinit var authentication: DeviceAuthentication
    private var pendingEnable = false
    private var updatingSwitch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        binding = ActivityBiometricsBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        pendingEnable = savedInstanceState?.getBoolean("pending_enable") ?: false
        authentication = DeviceAuthentication(this,
            onSuccess = {
                if (pendingEnable) {
                    pendingEnable = false
                    AppLock.setEnabled(this, true)
                    updateSwitch()
                }
            },
            onMessage = { binding.biometricsStatus.text = it }
        )
        binding.biometricsBack.setOnClickListener { finish() }
        binding.biometricsSwitch.setOnCheckedChangeListener { _, checked ->
            if (updatingSwitch) return@setOnCheckedChangeListener
            if (!checked) {
                pendingEnable = false
                AppLock.setEnabled(this, false)
                updateSwitch()
            } else {
                pendingEnable = true
                updateSwitch()
                if (authentication.hasDeviceLock) authentication.authenticate()
                else showDeviceLockInfo()
            }
        }
        updateSwitch()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) updateSwitch()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("pending_enable", pendingEnable)
        super.onSaveInstanceState(outState)
    }

    private fun updateSwitch() {
        val enabled = AppLock.isEnabled(this)
        updatingSwitch = true
        binding.biometricsSwitch.isChecked = enabled
        updatingSwitch = false
        binding.biometricsStatus.setText(if (enabled) R.string.biometrics_on else R.string.biometrics_off)
        if (enabled) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun showDeviceLockInfo() {
        pendingEnable = false
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.biometrics_title)
            .setMessage(R.string.biometrics_device_lock_required)
            .setPositiveButton(R.string.biometrics_open_security) { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                } catch (_: ActivityNotFoundException) {
                    binding.biometricsStatus.setText(R.string.authentication_unavailable)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

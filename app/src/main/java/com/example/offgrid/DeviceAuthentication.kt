package com.example.offgrid

import android.app.KeyguardManager
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class AuthenticationState : ViewModel() {
    var running = false
    var hasAttempted = false
}

internal class DeviceAuthentication(
    private val activity: AppCompatActivity,
    private val onSuccess: () -> Unit,
    private val onMessage: (CharSequence) -> Unit
) {
    private val keyguard = activity.getSystemService(KeyguardManager::class.java)
    private val state = ViewModelProvider(activity)[AuthenticationState::class.java]
    val hasAttempted: Boolean get() = state.hasAttempted
    val hasDeviceLock: Boolean get() = keyguard?.isDeviceSecure == true

    private val prompt = BiometricPrompt(
        activity, ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                state.running = false
                state.hasAttempted = true
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    onSuccess()
                } else {
                    state.hasAttempted = false
                    onMessage(activity.getString(R.string.authentication_cancelled))
                }
            }

            override fun onAuthenticationFailed() {
                onMessage(activity.getString(R.string.authentication_not_recognized))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                state.running = false
                state.hasAttempted = activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                val message = when (errorCode) {
                    BiometricPrompt.ERROR_CANCELED,
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> R.string.authentication_cancelled
                    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> R.string.biometrics_device_lock_required
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> R.string.authentication_locked_out
                    else -> R.string.authentication_unavailable
                }
                onMessage(activity.getString(message))
            }
        }
    )

    fun authenticate() {
        if (state.running || !activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        state.hasAttempted = true
        if (!hasDeviceLock) {
            onMessage(activity.getString(R.string.biometrics_device_lock_required))
            return
        }
        state.running = true
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.authentication_title))
            .setSubtitle(activity.getString(R.string.authentication_subtitle))
            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build())
    }

    fun prepareForNextOpen() {
        if (!state.running) state.hasAttempted = false
    }
}

package com.example.offgrid

import android.app.Activity
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate

class OffGridApplication : Application(),
    Application.ActivityLifecycleCallbacks,
    SensorEventListener {

    private val settings by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    private val sensorManager by lazy { getSystemService(SensorManager::class.java) }
    private val lightSensor by lazy { sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) }

    private var visibleActivities = 0
    private var listening = false

    internal val isInForeground: Boolean
        get() = visibleActivities > 0

    internal val isAutomaticTheme: Boolean
        get() = settings.getBoolean(AUTO_THEME, false)

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        if (settings.contains(DARK_MODE) && (!isAutomaticTheme || lightSensor != null)) {
            applyTheme(dark = settings.getBoolean(DARK_MODE, false))
        }
    }

    internal fun saveAutomaticTheme() {
        settings.edit().putBoolean(AUTO_THEME, true).apply()
        updateLightSensor()
    }

    private fun updateLightSensor() {
        if (visibleActivities == 0 || !isAutomaticTheme) {
            if (listening) sensorManager?.unregisterListener(this)
            listening = false
            return
        }

        if (listening) return

        val sensor = lightSensor
        listening = sensor != null && sensorManager?.registerListener(
            this, sensor, SensorManager.SENSOR_DELAY_NORMAL
        ) == true

        if (!listening) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!listening) return

        val lux = event.values[0]

        if (lux in 0f..50f) {
            applyTheme(dark = true)
        } else if (lux.isFinite() && lux >= 100f) {
            applyTheme(dark = false)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    override fun onActivityStarted(activity: Activity) {
        visibleActivities++
        updateLightSensor()
    }

    override fun onActivityStopped(activity: Activity) {
        visibleActivities--
        updateLightSensor()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    companion object {
        private const val PREFS = "offgrid_settings"
        private const val DARK_MODE = "dark_mode"
        private const val AUTO_THEME = "auto_theme"

        fun saveTheme(context: Context, dark: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(DARK_MODE, dark)
                .remove(AUTO_THEME)
                .apply()

            (context.applicationContext as? OffGridApplication)?.updateLightSensor()
            applyTheme(dark)
        }

        private fun applyTheme(dark: Boolean) {
            val mode = if (dark) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}

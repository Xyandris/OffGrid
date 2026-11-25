/**
 * @file OffGridApplication.kt
 * @brief Główna klasa aplikacji – obsługa motywu i czujnika światła.
 */
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

/**
 * @brief Zarządza motywem aplikacji (jasny/ciemny) na podstawie czujnika światła.
 *
 * Śledzi widoczność ekranów, żeby wyłączać nasłuch czujnika, gdy aplikacja idzie w tło.
 */
class OffGridApplication : Application(),
    Application.ActivityLifecycleCallbacks,
    SensorEventListener {

    /** @brief Ustawienia aplikacji w SharedPreferences. */
    private val settings by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    /** @brief Systemowy menedżer czujników. */
    private val sensorManager by lazy { getSystemService(SensorManager::class.java) }

    /** @brief Czujnik światła (null, jeśli urządzenie go nie ma). */
    private val lightSensor by lazy { sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) }

    /** @brief Licznik aktualnie widocznych aktywności. */
    private var visibleActivities = 0

    /** @brief Czy aktualnie nasłuchujemy zmian z czujnika światła. */
    private var listening = false

    /**
     * @brief Sprawdza, czy włączono automatyczny dobór motywu.
     * @return true, jeśli tryb auto jest aktywny.
     */
    internal val isAutomaticTheme: Boolean
        get() = settings.getBoolean(AUTO_THEME, false)

    /**
     * @brief Rejestruje callbacki cyklu życia i ustawia zapisany motyw przy starcie.
     */
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        if (settings.contains(DARK_MODE) && (!isAutomaticTheme || lightSensor != null)) {
            applyTheme(dark = settings.getBoolean(DARK_MODE, false))
        }
    }

    /**
     * @brief Włącza automatyczny motyw, zapisuje to w ustawieniach i odpala czujnik.
     */
    internal fun saveAutomaticTheme() {
        settings.edit().putBoolean(AUTO_THEME, true).apply()
        updateLightSensor()
    }

    /**
     * @brief Włącza lub wyłącza nasłuch czujnika światła zależnie od widoczności aplikacji i ustawień.
     *
     * Jeśli brakuje czujnika, ustawia motyw zgodny z systemowym.
     */
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

    /**
     * @brief Zmienia motyw przy zmianie natężenia światła (do 50 lx -> ciemny, od 100 lx -> jasny).
     * @param event Odczyt z czujnika światła.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (!listening) return

        val lux = event.values[0]

        if (lux in 0f..50f) {
            applyTheme(dark = true)
        } else if (lux.isFinite() && lux >= 100f) {
            applyTheme(dark = false)
        }
    }

    /**
     * @brief Nieużywane – wymagane przez SensorEventListener.
     * @param sensor Czujnik.
     * @param accuracy Dokładność.
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    /**
     * @brief Zwiększa licznik widocznych ekranów i wznawia nasłuch czujnika.
     * @param activity Uruchomiona aktywność.
     */
    override fun onActivityStarted(activity: Activity) {
        visibleActivities++
        updateLightSensor()
    }

    /**
     * @brief Zmniejsza licznik widocznych ekranów i wyłącza czujnik, jeśli aplikacja jest w tle.
     * @param activity Zatrzymana aktywność.
     */
    override fun onActivityStopped(activity: Activity) {
        visibleActivities--
        updateLightSensor()
    }

    /**
     * @brief Nieużywane.
     * @param activity Aktywność.
     * @param savedInstanceState Zapisany stan.
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    /**
     * @brief Nieużywane.
     * @param activity Aktywność.
     */
    override fun onActivityResumed(activity: Activity) = Unit

    /**
     * @brief Nieużywane.
     * @param activity Aktywność.
     */
    override fun onActivityPaused(activity: Activity) = Unit

    /**
     * @brief Nieużywane.
     * @param activity Aktywność.
     * @param outState Paczka ze stanem.
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    /**
     * @brief Nieużywane.
     * @param activity Aktywność.
     */
    override fun onActivityDestroyed(activity: Activity) = Unit

    /**
     * @brief Stałe do SharedPreferences i metody do ręcznej zmiany motywu.
     */
    companion object {
        /** @brief Nazwa pliku z ustawieniami. */
        private const val PREFS = "offgrid_settings"

        /** @brief Klucz zapisanego trybu ciemnego. */
        private const val DARK_MODE = "dark_mode"

        /** @brief Klucz trybu automatycznego. */
        private const val AUTO_THEME = "auto_theme"

        /**
         * @brief Zapisuje wybrany motyw na stałe (wyłącza tryb auto) i od razu go ustawia.
         * @param context Kontekst aplikacji.
         * @param dark true dla ciemnego motywu, false dla jasnego.
         */
        fun saveTheme(context: Context, dark: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(DARK_MODE, dark)
                .remove(AUTO_THEME)
                .apply()

            (context.applicationContext as? OffGridApplication)?.updateLightSensor()
            applyTheme(dark)
        }

        /**
         * @brief Ustawia tryb nocny w AppCompat.
         * @param dark true = tryb ciemny, false = tryb jasny.
         */
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
/**
 * @file app/src/main/java/com/example/offgrid/OffGridApplication.kt
 * @brief Inicjalizacja aplikacji, profili Firestore oraz obsługi motywu.
 * @details Łączy obserwację sesji użytkownika z cyklem życia aktywności i czujnikiem oświetlenia.
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
 * @brief Zarządza wspólnym stanem aplikacji OffGrid.
 * @details Inicjalizuje UserProfiles, śledzi aktywności na pierwszym planie i dopasowuje motyw
 *          do zapisanego wyboru albo natężenia światła. Po opuszczeniu aplikacji włącza blokadę.
 * @see UserProfiles
 */
class OffGridApplication : Application(),
    Application.ActivityLifecycleCallbacks,
    SensorEventListener {

    /**
     * @brief Preferencje przechowujące ręczny i automatyczny wybór motywu.
     */
    private val settings by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    /**
     * @brief Systemowy menedżer czujników urządzenia.
     */
    private val sensorManager by lazy { getSystemService(SensorManager::class.java) }
    /**
     * @brief Opcjonalny czujnik natężenia światła używany przez motyw automatyczny.
     */
    private val lightSensor by lazy { sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) }

    /**
     * @brief Liczba uruchomionych aktywności; wartość dodatnia oznacza aplikację na pierwszym planie.
     */
    private var visibleActivities = 0
    /**
     * @brief Informacja, czy aplikacja jest zarejestrowana jako odbiorca pomiarów światła.
     */
    private var listening = false

    /**
     * @brief Wskazuje, czy co najmniej jedna aktywność pozostaje uruchomiona.
     */
    internal val isInForeground: Boolean
        get() = visibleActivities > 0

    /**
     * @brief Wskazuje, czy użytkownik wybrał automatyczne przełączanie motywu.
     */
    internal val isAutomaticTheme: Boolean
        get() = settings.getBoolean(AUTO_THEME, false)

    /**
     * @brief Przygotowuje globalne usługi aplikacji.
     * @details Rejestruje obserwację aktywności, uruchamia synchronizację profili i odtwarza motyw.
     */
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        UserProfiles.initialize(this)

        if (settings.contains(DARK_MODE) && (!isAutomaticTheme || lightSensor != null)) {
            applyTheme(dark = settings.getBoolean(DARK_MODE, false))
        }
    }

    /**
     * @brief Włącza automatyczny motyw i aktualizuje nasłuchiwanie czujnika światła.
     */
    internal fun saveAutomaticTheme() {
        settings.edit().putBoolean(AUTO_THEME, true).apply()
        updateLightSensor()
    }

    /**
     * @brief Dopasowuje rejestrację czujnika do widoczności aplikacji i ustawień motywu.
     * @details Przy braku dostępnego czujnika lub nieudanej rejestracji wybiera motyw systemowy.
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
     * @brief Przełącza motyw po przekroczeniu progów oświetlenia.
     * @param event Pomiar czujnika; pierwsza wartość zawiera natężenie światła w luksach.
     * @details Zakres 0–50 lx wybiera ciemny motyw, a wartości od 100 lx wybierają jasny.
     *          Przerwa między progami ogranicza częste przełączanie przy zbliżonych pomiarach.
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
     * @brief Przyjmuje zmianę dokładności czujnika bez dodatkowej obsługi.
     * @param sensor Czujnik zgłaszający zmianę.
     * @param accuracy Nowy poziom dokładności; nie jest wykorzystywany do wyboru motywu.
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    /**
     * @brief Zwiększa licznik aktywności i w razie potrzeby uruchamia czujnik.
     * @param activity Aktywność przechodząca do stanu uruchomionego.
     */
    override fun onActivityStarted(activity: Activity) {
        visibleActivities++
        updateLightSensor()
    }

    /**
     * @brief Aktualizuje licznik aktywności, blokadę aplikacji i rejestrację czujnika.
     * @param activity Zatrzymywana aktywność; jej zmiana konfiguracji nie powoduje zablokowania sesji.
     */
    override fun onActivityStopped(activity: Activity) {
        visibleActivities = (visibleActivities - 1).coerceAtLeast(0)
        if (visibleActivities == 0 && !activity.isChangingConfigurations) AppLock.lock()
        updateLightSensor()
    }

    /**
     * @brief Przyjmuje powiadomienie o utworzeniu aktywności bez dodatkowej obsługi.
     * @param activity Utworzona aktywność.
     * @param savedInstanceState Odtwarzany stan aktywności albo null.
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    /**
     * @brief Przyjmuje powiadomienie o wznowieniu aktywności bez dodatkowej obsługi.
     * @param activity Wznowiona aktywność.
     */
    override fun onActivityResumed(activity: Activity) = Unit
    /**
     * @brief Przyjmuje powiadomienie o wstrzymaniu aktywności bez dodatkowej obsługi.
     * @param activity Wstrzymana aktywność.
     */
    override fun onActivityPaused(activity: Activity) = Unit
    /**
     * @brief Przyjmuje powiadomienie o zapisie stanu bez dodawania własnych danych.
     * @param activity Aktywność zapisująca stan.
     * @param outState Pakiet stanu przekazany przez system.
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    /**
     * @brief Przyjmuje powiadomienie o zniszczeniu aktywności bez dodatkowej obsługi.
     * @param activity Zniszczona aktywność.
     */
    override fun onActivityDestroyed(activity: Activity) = Unit

    /**
     * @brief Udostępnia zapis ręcznego wyboru motywu i klucze preferencji.
     */
    companion object {
        /**
         * @brief Nazwa pliku preferencji ustawień OffGrid.
         */
        private const val PREFS = "offgrid_settings"
        /**
         * @brief Klucz zapamiętanego wyboru ciemnego motywu.
         */
        private const val DARK_MODE = "dark_mode"
        /**
         * @brief Klucz włączenia motywu sterowanego automatycznie.
         */
        private const val AUTO_THEME = "auto_theme"

        /**
         * @brief Zapisuje ręczny wybór motywu i wyłącza tryb automatyczny.
         * @param context Kontekst używany do odczytu preferencji i instancji aplikacji.
         * @param dark Wartość true wybiera motyw ciemny, a false wybiera jasny.
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
         * @brief Ustawia wybrany wariant motywu we wszystkich aktywnościach AppCompat.
         * @param dark Wartość true oznacza motyw ciemny, a false oznacza jasny.
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

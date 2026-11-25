/**
 * @file offgrid/OffGridApplication.kt
 * @brief Główna klasa aplikacji OffGrid zarządzająca cyklem życia aktywności, blokadą i motywem.
 * @details Monitoruje widoczność aktywności w celu blokowania sesji po przejściu aplikacji w tło
 *oraz dostosowuje motyw jasny lub ciemny na podstawie ustawień użytkownika lub czujnika światła.
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
 * @brief Klasa `Application` koordynująca stan pierwszego planu oraz automatyczny i ręczny motyw OffGrid.
 */
class OffGridApplication : Application(),
    Application.ActivityLifecycleCallbacks,
    SensorEventListener {

    /**
     * @brief Preferencje `SharedPreferences` przechowujące konfigurację motywu aplikacji.
     */
    private val settings by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    /**
     * @brief Systemowy menedżer czujników wykorzystywany do dostępu do czujnika światła.
     */
    private val sensorManager by lazy { getSystemService(SensorManager::class.java) }

    /**
     * @brief Domyślny czujnik natężenia oświetlenia (`Sensor.TYPE_LIGHT`) lub `null`, gdy jest niedostępny.
     */
    private val lightSensor by lazy { sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT) }

    /**
     * @brief Liczba aktualnie widocznych aktywności aplikacji.
     */
    private var visibleActivities = 0

    /**
     * @brief Flaga określająca, czy aplikacja nasłuchuje obecnie zdarzeń czujnika światła.
     */
    private var listening = false

    /**
     * @brief Wskazuje, czy co najmniej jedna aktywność aplikacji znajduje się na pierwszym planie.
     */
    internal val isInForeground: Boolean
        get() = visibleActivities > 0

    /**
     * @brief Wskazuje, czy aktywny jest automatyczny tryb doboru motywu na podstawie oświetlenia otoczenia.
     */
    internal val isAutomaticTheme: Boolean
        get() = settings.getBoolean(AUTO_THEME, false)

    /**
     * @brief Inicjalizuje aplikację, rejestruje obserwator cyklu życia aktywności i stosuje zapisany motyw.
     */
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        if (settings.contains(DARK_MODE) && (!isAutomaticTheme || lightSensor != null)) {
            applyTheme(dark = settings.getBoolean(DARK_MODE, false))
        }
    }

    /**
     * @brief Zapisuje wybór automatycznego motywu w preferencjach i uruchamia obsługę czujnika światła.
     */
    internal fun saveAutomaticTheme() {
        settings.edit().putBoolean(AUTO_THEME, true).apply()
        updateLightSensor()
    }

    /**
     * @brief Zarządza rejestracją i wyrejestrowaniem nasłuchiwania czujnika światła otoczenia.
     * @details Rejestruje czujnik wyłącznie wtedy, gdy aplikacja jest widoczna i włączony jest tryb
     *automatyczny. W przypadku braku czujnika ustawia tryb `MODE_NIGHT_FOLLOW_SYSTEM`.
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
     * @brief Zmienia motyw aplikacji w odpowiedzi na odczyt natężenia oświetlenia z czujnika.
     * @details Dla wartości z przedziału `0..50` lx włącza motyw ciemny, a od `100` lx wzwyż motyw jasny.
     * @param event Zdarzenie czujnika zawierające aktualny pomiar w luksach.
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
     * @brief Obsługuje zmianę dokładności czujnika; niewykorzystywane w tej aplikacji.
     * @param sensor Czujnik, którego dokładność uległa zmianie.
     * @param accuracy Nowy poziom dokładności czujnika.
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    /**
     * @brief Zwiększa licznik widocznych aktywności i aktualizuje nasłuchiwanie czujnika światła.
     * @param activity Aktywność, która została uruchomiona.
     */
    override fun onActivityStarted(activity: Activity) {
        visibleActivities++
        updateLightSensor()
    }

    /**
     * @brief Zmniejsza licznik widocznych aktywności i blokuje sesję po przejściu aplikacji w tło.
     * @details Gdy liczba widocznych aktywności spadnie do zera i nie zachodzi zmiana konfiguracji,
     * wywołuje `AppLock.lock()`, a następnie wyrejestrowuje czujnik światła.
     * @param activity Aktywność, która została zatrzymana.
     */
    override fun onActivityStopped(activity: Activity) {
        visibleActivities = (visibleActivities - 1).coerceAtLeast(0)
        if (visibleActivities == 0 && !activity.isChangingConfigurations) AppLock.lock()
        updateLightSensor()
    }

    /**
     * @brief Wywoływane przy tworzeniu aktywności; brak dodatkowej logiki.
     * @param activity Tworzona aktywność.
     * @param savedInstanceState Zapisany stan aktywności lub `null`.
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    /**
     * @brief Wywoływane przy wznowieniu aktywności; brak dodatkowej logiki.
     * @param activity Wznawiana aktywność.
     */
    override fun onActivityResumed(activity: Activity) = Unit

    /**
     * @brief Wywoływane przy wstrzymaniu aktywności; brak dodatkowej logiki.
     * @param activity Wstrzymywana aktywność.
     */
    override fun onActivityPaused(activity: Activity) = Unit

    /**
     * @brief Wywoływane przy zapisywaniu stanu instancji aktywności; brak dodatkowej logiki.
     * @param activity Aktywność zapisująca stan.
     * @param outState Kontener na zapisywany stan.
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    /**
     * @brief Wywoływane przy niszczeniu aktywności; brak dodatkowej logiki.
     * @param activity Niszczona aktywność.
     */
    override fun onActivityDestroyed(activity: Activity) = Unit

    /**
     * @brief Stałe preferencji oraz metody pomocnicze do zapisywania i stosowania motywu aplikacji.
     */
    companion object {
        /**
         * @brief Nazwa pliku `SharedPreferences` z ustawieniami aplikacji.
         */
        private const val PREFS = "offgrid_settings"

        /**
         * @brief Klucz preferencji określający wybór motywu ciemnego.
         */
        private const val DARK_MODE = "dark_mode"

        /**
         * @brief Klucz preferencji określający włączenie trybu automatycznego motywu.
         */
        private const val AUTO_THEME = "auto_theme"

        /**
         * @brief Zapisuje ręczny wybór motywu jasnego lub ciemnego i natychmiast go stosuje.
         * @details Wyłącza tryb automatyczny, aktualizuje nasłuchiwanie czujnika światła w instancji
         *`OffGridApplication` oraz ustawia odpowiedni tryb nocy w `AppCompatDelegate`.
         * @param context Kontekst używany do zapisu preferencji i pobrania instancji aplikacji.
         * @param dark Wartość `true` dla motywu ciemnego lub `false` dla motywu jasnego.
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
         * @brief Ustawia domyślny tryb nocy w `AppCompatDelegate`.
         * @param dark Wartość `true` dla `MODE_NIGHT_YES` lub `false` dla `MODE_NIGHT_NO`.
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

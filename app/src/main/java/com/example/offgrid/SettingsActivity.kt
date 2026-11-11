package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivitySettingsBinding
import android.content.res.Configuration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.View
import com.google.firebase.auth.FirebaseAuth

/**
 * @brief Ekran ustawień z wejściem do deweloperskich formularzy konta.
 * Obsługuje zakładki, powrót oraz otwieranie logowania i rejestracji.
 * @note Pozostałe pozycje ustawień i wylogowanie są na razie elementami makiety.
 */
class SettingsActivity : AppCompatActivity() {
    /**
     * @brief Inicjalizuje nawigację ustawień i przyciski otwierające formularze konta.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    private lateinit var binding: ActivitySettingsBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val accountListener= FirebaseAuth.AuthStateListener{firebase->
        val user=firebase.currentUser
        val email=user?.email.orEmpty()
        val name=user?.displayName?.takeIf {it.isNotBlank()}
        val profile = binding.settingsScreen

        profile.settingsProfileName.text = when {
            user == null -> getString(R.string.settings_account_signed_out)
            name != null -> name
            else -> "Name not set"
        }

        profile.settingsProfileEmail.text = email
        profile.settingsProfileEmail.visibility =
            if (email.isBlank()) View.GONE else View.VISIBLE

        profile.settingsProfileAvatar.text =
            (name ?: email).firstOrNull()?.uppercase() ?: "?"
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(accountListener)
    }

    override fun onStop() {
        auth.removeAuthStateListener(accountListener)
        super.onStop()
    }
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        bindMainTabs(binding.settingsScreen.bottomNavigation, MainTab.SETTINGS)

        val screen = binding.settingsScreen
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val selected = if (nightMode == Configuration.UI_MODE_NIGHT_YES) 1 else 0
        val options = arrayOf(
            getString(R.string.appearance_light),
            getString(R.string.appearance_dark)
        )

        screen.settingsThemeValue.text = options[selected]

        screen.settingsAppearance.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.appearance_title)
                .setSingleChoiceItems(options, selected) { dialog, which ->
                    dialog.dismiss()
                    OffGridApplication.saveTheme(this, dark = which == 1)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        binding.settingsScreen.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.settingsScreen.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.settingsScreen.settingsBack.setOnClickListener { finish() }
        binding.settingsScreen.settingsLogout.setOnClickListener {
            auth.signOut()
        }
    }
}



package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivitySettingsBinding
import android.content.res.Configuration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.View
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

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
        screen.settingsPrivacy.setOnClickListener {
            showSettingsInfo(
                R.string.settings_privacy_title,
                getString(R.string.settings_privacy_message)
            )
        }
        screen.settingsNotifications.setOnClickListener {
            showSettingsInfo(
                R.string.settings_notifications_title,
                getString(R.string.settings_notifications_message)
            )
        }
        screen.settingsHelp.setOnClickListener {
            showSettingsInfo(
                R.string.settings_help_title,
                getString(R.string.settings_help_message)
            )
        }
        screen.settingsAbout.setOnClickListener {
            @Suppress("DEPRECATION")
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

            showSettingsInfo(
                R.string.settings_about_title,
                getString(R.string.settings_about_message, getString(R.string.app_name), versionName)
            )
        }
        val app = application as OffGridApplication
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val options = arrayOf(
            getString(R.string.appearance_light),
            getString(R.string.appearance_dark),
            getString(R.string.appearance_automatic)
        )

        var selected = when {
            app.isAutomaticTheme -> 2
            nightMode == Configuration.UI_MODE_NIGHT_YES -> 1
            else -> 0
        }

        screen.settingsThemeValue.text = options[selected]

        screen.settingsAppearance.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.appearance_title)
                .setSingleChoiceItems(options, selected) { dialog, which ->
                    dialog.dismiss()
                    selected = which
                    screen.settingsThemeValue.text = options[which]

                    if (which == 2) {
                        app.saveAutomaticTheme()
                    } else {
                        OffGridApplication.saveTheme(this, dark = which == 1)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        binding.settingsScreen.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).putExtra(DEVELOPER_SCREEN, true))
        }
        binding.settingsScreen.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).putExtra(DEVELOPER_SCREEN, true))
        }
        binding.settingsScreen.settingsBack.setOnClickListener { finish() }
        binding.settingsScreen.settingsLogout.setOnClickListener {
            auth.signOut()
        }

    }
    private fun showSettingsInfo(titleRes: Int, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}

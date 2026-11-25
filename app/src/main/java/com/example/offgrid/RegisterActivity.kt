package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)
        val form = binding.registerScreen
        val developerScreen = intent.getBooleanExtra(DEVELOPER_SCREEN, false)

        form.registerOpenLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)
                .putExtra(DEVELOPER_SCREEN, developerScreen))
            finish()
        }
        form.registerBack.setOnClickListener { goBack(developerScreen) }
        onBackPressedDispatcher.addCallback(this) { goBack(developerScreen) }
        form.registerSubmit.setOnClickListener {
            val name = form.registerName.text.toString().trim()
            val email = form.registerEmail.text.toString().trim()
            val password = form.registerPassword.text.toString()
            val confirmation = form.registerConfirmPassword.text.toString()
            if (name.isBlank()) {
                form.registerName.error = getString(R.string.auth_name_required)
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                password.length < 6 || password != confirmation) {
                Toast.makeText(this, R.string.auth_invalid_registration, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            form.registerSubmit.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    form.registerSubmit.isEnabled = true
                    Toast.makeText(this, R.string.auth_register_failed, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }
                val user = task.result?.user ?: return@addOnCompleteListener
                val profile = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                user.updateProfile(profile).addOnCompleteListener(this) { profileTask ->
                    if (!profileTask.isSuccessful) {
                        Toast.makeText(this, R.string.auth_profile_not_saved, Toast.LENGTH_LONG).show()
                    }
                    openChats()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null && !intent.getBooleanExtra(DEVELOPER_SCREEN, false)) {
            openChats()
        }
    }

    private fun goBack(developerScreen: Boolean) {
        if (developerScreen) finish() else openLogin()
    }
}

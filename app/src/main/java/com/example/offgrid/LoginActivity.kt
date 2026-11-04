package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)

        binding.loginScreen.loginOpenRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            // Replace this form so Back returns to Settings after switching forms.
            finish()
        }
        binding.loginScreen.loginBack.setOnClickListener { finish() }
        binding.loginScreen.loginSubmit.setOnClickListener {
            // Connect your authentication provider here.
            Toast.makeText(this, R.string.login_not_configured, Toast.LENGTH_SHORT).show()
        }
        binding.loginScreen.loginForgotPassword.setOnClickListener {
            Toast.makeText(this, R.string.password_reset_not_configured, Toast.LENGTH_SHORT).show()
        }
    }
}

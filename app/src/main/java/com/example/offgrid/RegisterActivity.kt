package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)

        binding.registerScreen.registerOpenLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.registerScreen.registerBack.setOnClickListener { finish() }
        binding.registerScreen.registerSubmit.setOnClickListener {
            // Connect your authentication provider here to create an account.
            Toast.makeText(this, R.string.register_not_configured, Toast.LENGTH_SHORT).show()
        }
    }
}

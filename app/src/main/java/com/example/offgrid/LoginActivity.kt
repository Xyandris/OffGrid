package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyScreenInsets(binding.root)

        binding.btnOpenRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            // Replace this form so Back returns to Settings after switching forms.
            finish()
        }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSubmitLogin.setOnClickListener {
            // Connect your authentication provider here.
            Toast.makeText(this, R.string.login_not_configured, Toast.LENGTH_SHORT).show()
        }
    }
}

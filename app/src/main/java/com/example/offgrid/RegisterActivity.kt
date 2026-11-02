package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyScreenInsets(binding.root)

        binding.btnOpenLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSubmitRegister.setOnClickListener {
            // Connect your authentication provider here to create an account.
            Toast.makeText(this, R.string.register_not_configured, Toast.LENGTH_SHORT).show()
        }
    }
}

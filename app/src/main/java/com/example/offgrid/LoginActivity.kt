package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * @brief Ekran formularza logowania otwierany z sekcji deweloperskiej ustawień.
 *
 * Przejście do rejestracji kończy tę aktywność, aby powrót nie odtwarzał kolejnych formularzy na stosie.
 * @note Logowanie i reset hasła wyświetlają wyłącznie komunikaty o braku konfiguracji.
 */
class LoginActivity : AppCompatActivity() {
    /**
     * @brief Podłącza powrót, przejście do rejestracji oraz komunikaty zastępujące uwierzytelnianie.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        showOffGridScreen(binding.root)

        binding.loginScreen.loginOpenRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            // Zamknij logowanie, aby z rejestracji można było wrócić bezpośrednio do ustawień.
            finish()
        }
        binding.loginScreen.loginBack.setOnClickListener { finish() }
        binding.loginScreen.loginSubmit.setOnClickListener {
            val form = binding.loginScreen
            val email = form.loginEmail.text.toString().trim()
            val password = form.loginPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            form.loginSubmit.isEnabled = false

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task ->
                    form.loginSubmit.isEnabled=true
                    if(task.isSuccessful){
                        Toast.makeText(this,"Logged In", Toast.LENGTH_SHORT).show()
                        val intent=Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        }else{
                            Toast.makeText(this,"Wrong password or email. Tryb again",Toast.LENGTH_LONG).show()

                    }
                }
        }
        binding.loginScreen.loginForgotPassword.setOnClickListener {
            Toast.makeText(this, R.string.password_reset_not_configured, Toast.LENGTH_SHORT).show()
        }
    }
}

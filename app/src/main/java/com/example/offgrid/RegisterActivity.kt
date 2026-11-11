package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * @brief Ekran formularza rejestracji dostępny z ustawień lub formularza logowania.
 * Przejście do logowania kończy bieżącą aktywność, zachowując możliwość powrotu bezpośrednio do ustawień.
 * @note Przycisk rejestracji wyświetla komunikat.
 */
class RegisterActivity : AppCompatActivity() {
    /**
     * @brief Podłącza przejście do logowania, zamknięcie formularza i komunikat rejestracji.
     * @param savedInstanceState Stan zapisany przez Androida lub null przy pierwszym utworzeniu.
     */
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
            val form=binding.registerScreen
            val email=form.registerEmail.text.toString().trim()
            val password=form.registerPassword.text.toString()
            val confirmation=form.registerConfirmPassword.text.toString()

            if(email.isBlank() || password.isBlank() || password!=confirmation){
                Toast.makeText(this,"Enter email and correct password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            form.registerSubmit.isEnabled=false
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) {task->
                    form.registerSubmit.isEnabled=true

                    if(task.isSuccessful){
                        Toast.makeText(this,"Account is created", Toast.LENGTH_SHORT).show()
                        finish()
                    }else{
                        val message=task.exception?.localizedMessage
                            ?:"It's not possible to create your account"
                        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}

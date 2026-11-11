package com.example.offgrid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.offgrid.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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
            val name = form.registerName.text.toString().trim()
            if (name.isBlank()) {
                form.registerName.error = "Enter account name"
                return@setOnClickListener
            }
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
                        val user = task.result?.user ?: return@addOnCompleteListener
                        form.registerSubmit.isEnabled = false
                        val profile= UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user.updateProfile(profile).addOnCompleteListener(this) { result ->
                            val message = if (result.isSuccessful) {
                                "Account is created and name is saved"
                            } else {
                                "Account is created. Name creation is not successful"
                            }
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                            finish()
                        }


                    }else{
                        val message=task.exception?.localizedMessage
                            ?:"It's not possible to create your account"
                        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}

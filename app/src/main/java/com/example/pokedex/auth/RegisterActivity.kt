package com.example.pokedex.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pokedex.R
import com.example.pokedex.ui.main.MainActivity
import com.example.pokedex.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_gray)

        super.onCreate(savedInstanceState)

        // Configurar View Binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Botón de registro
        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    registerUser(email, password)
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Enlace para ir a la pantalla de inicio de sesión
        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Obtener el ID del usuario recién registrado
                    val userId = auth.currentUser?.uid ?: ""
                    if (userId.isNotEmpty()) {
                        // Crear el documento del usuario en Firestore
                        createUserDocument(userId)
                    }

                    // Ir a la actividad principal
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserDocument(userId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        // Datos iniciales del usuario
        val userData = hashMapOf(
            "favorites" to mutableListOf<Int>(), // Lista vacía de favoritos
            "captured" to mutableListOf<Int>()   // Lista vacía de capturados
        )

        // Crea el documento si no existe
        userRef.set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Documento del usuario creado: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "Error al crear el documento del usuario: ${e.message}")
            }
    }
}
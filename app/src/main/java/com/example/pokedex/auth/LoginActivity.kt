package com.example.pokedex.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pokedex.R
import com.example.pokedex.ui.main.MainActivity
import com.example.pokedex.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_gray)

        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Configurar View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Botón de inicio de sesión
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Enlace para ir a la pantalla de registro
        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Obtener el ID del usuario que inició sesión
                    val userId = auth.currentUser?.uid ?: ""
                    if (userId.isNotEmpty()) {
                        // Verificar si el documento del usuario existe en Firestore
                        checkAndCreateUserDocument(userId)
                    }

                    // Ir a la actividad principal
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndCreateUserDocument(userId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // Si el documento no existe, créalo
                createUserDocument(userId)
            }
        }.addOnFailureListener { e ->
            Log.e("LoginActivity", "Error al verificar el documento del usuario: ${e.message}")
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
                Log.d("LoginActivity", "Documento del usuario creado: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Error al crear el documento del usuario: ${e.message}")
            }
    }
}
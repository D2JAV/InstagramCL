package com.example.instagramcl

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
class LoginActivity : AppCompatActivity() {

  /*
  * usuario de prueba: test@example.com
  * contraseña: password123
  * */

  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    // Inicializar Firebase Auth
    auth = FirebaseAuth.getInstance()

    // Vincular vistas
    val emailEditText = findViewById<EditText>(R.id.emailEditText)
    val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
    val loginButton = findViewById<Button>(R.id.loginButton)
    val registerButton = findViewById<Button>(R.id.registerButton)

    // Listener del botón de inicio de sesión
    loginButton.setOnClickListener {
      val email = emailEditText.text.toString()
      val password = passwordEditText.text.toString()

      if (email.isNotEmpty() && password.isNotEmpty()) {
        auth.signInWithEmailAndPassword(email, password)
          .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
              // Inicio de sesión exitoso, navegar a la siguiente pantalla
              Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
              val intent = Intent(this, FeedActivity::class.java)
              intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Para que no pueda volver al login con "atrás"
              startActivity(intent)
              finish() // Cierra LoginActivity
            } else {
              // Si falla, mostrar un mensaje de error
              Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
          }
      } else {
        Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
      }
    }

    // Listener para el botón de registrarse (te llevará a la siguiente pantalla que crearemos)
    registerButton.setOnClickListener {
      val intent = Intent(this, RegisterActivity::class.java)
      startActivity(intent)
     // Toast.makeText(this, "Navegando a la pantalla de registro...", Toast.LENGTH_SHORT).show()
    }
  }

}
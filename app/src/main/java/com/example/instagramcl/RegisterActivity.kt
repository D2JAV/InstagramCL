package com.example.instagramcl

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

  private lateinit var auth: FirebaseAuth
 private lateinit var db: FirebaseFirestore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)

    auth = FirebaseAuth.getInstance()
    db = FirebaseFirestore.getInstance()

    val emailEditText = findViewById<EditText>(R.id.registerEmailEditText)
    val passwordEditText = findViewById<EditText>(R.id.registerPasswordEditText)
    val confirmPasswordEditText = findViewById<EditText>(R.id.registerConfirmPasswordEditText)
    val usernameEditText = findViewById<EditText>(R.id.registerUsernameEditText) // Opcional
    val registerButton = findViewById<Button>(R.id.registerButton)
    val loginLinkTextView = findViewById<TextView>(R.id.tvLoginLink)

    registerButton.setOnClickListener {
      val email = emailEditText.text.toString().trim()
      val password = passwordEditText.text.toString().trim()
      val confirmPassword = confirmPasswordEditText.text.toString().trim()
      val username = usernameEditText.text.toString().trim() // Opcional

      if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        Toast.makeText(this, "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }

      if (password != confirmPassword) {
        Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }

      if (password.length < 6) {
        Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }

      // Crear usuario con Firebase Authentication
      auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this) { task ->
          if (task.isSuccessful) {
            // Registro exitoso
            val firebaseUser = auth.currentUser
            Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_SHORT).show()

            // Opcional: Actualizar el perfil del usuario con el nombre de usuario
            if (firebaseUser != null && username.isNotEmpty()) {
              val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                // Puedes añadir una URL de foto aquí también si la tienes
                // .setPhotoUri(Uri.parse("url_de_la_foto"))
                .build()

              firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener { profileTask ->
                  if (profileTask.isSuccessful) {
                    // Nombre de usuario actualizado en Firebase Auth
                  }
                }
            }


             if (firebaseUser != null) {
                val userMap = hashMapOf(
                    "uid" to firebaseUser.uid,
                    "email" to email,
                    "username" to username, // Si lo tienes
                    "profileImageUrl" to "" // URL de imagen de perfil inicial
                    // ... otros campos que necesites
                )
                db.collection("users").document(firebaseUser.uid)
                    .set(userMap)
                    .addOnSuccessListener { Log.d("Firestore", "Usuario guardado en Firestore") }
                    .addOnFailureListener { e -> Log.w("Firestore", "Error al guardar usuario", e) }
            }


            // Navegar a la pantalla de inicio de sesión o al feed principal
            // Por ahora, volvamos al Login para que el usuario inicie sesión manualmente
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Limpia el stack de actividades
            startActivity(intent)
            finish() // Cierra RegisterActivity

          } else {
            // Si el registro falla, muestra un mensaje al usuario.
            Toast.makeText(baseContext, "Fallo en el registro: ${task.exception?.message}",
              Toast.LENGTH_LONG).show()
          }
        }
    }

    loginLinkTextView.setOnClickListener {
      // Navegar de vuelta a LoginActivity
      finish() // Cierra la actividad actual (RegisterActivity)
    }
  }
}
    
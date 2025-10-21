package com.example.instagramcl

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.FirebaseStorage

// Importa el modelo de datos que creaste
import com.example.instagramcl.Post

class PublishPostActivity : AppCompatActivity() {

  private lateinit var imageViewSelected: ImageView
  private lateinit var editTextCaption: EditText
 // private lateinit var editTextLocation: EditText
  private lateinit var buttonShare: Button
  private lateinit var buttonBack: ImageButton

  private var selectedImageUri: Uri? = null

  // Referencias de Firebase
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
  //private val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://TU_STORAGE_URL_AQUI.appspot.com")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_publish_post)

    // 1. Inicializar Vistas
    imageViewSelected = findViewById(R.id.imageViewSelected)
    editTextCaption = findViewById(R.id.editTextCaption)
   // editTextLocation = findViewById(R.id.editTextLocation)
    buttonShare = findViewById(R.id.buttonShare)
    buttonBack = findViewById(R.id.buttonBack)

    // 2. Cargar la URI de la imagen y previsualizarla
    val imageUriString = intent.getStringExtra("IMAGE_URI")
    if (imageUriString != null) {
      selectedImageUri = Uri.parse(imageUriString)
      Glide.with(this).load(selectedImageUri).centerCrop().into(imageViewSelected)
    } else {
      Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
      finish()
      return
    }

    // 3. Configurar Listeners
    buttonBack.setOnClickListener { finish() }
    buttonShare.setOnClickListener { sharePost() }
  }

  private fun sharePost() {
    // Deshabilitar el botón y mostrar un mensaje de carga
    buttonShare.isEnabled = false
    buttonShare.text = "Publicando..."

    val currentUserId = auth.currentUser?.uid
    // Ya no necesitamos selectedImageUri para la subida, solo para la comprobación

    if (currentUserId == null) {
      Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
      resetShareButton()
      return
    }

    // 1. OBTENER LA URL DEL PLACEHOLDER EN LUGAR DE SUBIR LA IMAGEN
    val placeholderImageUrl = getPlaceholderImageUrl(currentUserId)

    // 2. Llamar directamente a savePostToFirestore con la URL falsa
    savePostToFirestore(currentUserId, placeholderImageUrl)

    // NOTA: Elimina la función uploadImageToStorage por completo, ya no se usa.
  }

  private fun getPlaceholderImageUrl(userId: String): String {
    // Usamos el ID de usuario y el tiempo para que cada "imagen" sea única.
    val uniqueSeed = "${userId}_${System.currentTimeMillis()}"
    return "https://picsum.photos/seed/${uniqueSeed}/500/500"
  }

  private fun savePostToFirestore(userId: String, imageUrl: String) {
    // Aquí debes obtener el nombre de usuario y URL de perfil
    // desde tu colección 'users' de Firestore. Usamos placeholders por ahora.
    val username = auth.currentUser?.email?.substringBefore('@') ?: "Grok"
    val userProfileImageUrl = "https://picsum.photos/seed/instagram/500/500" // Placeholder o URL real

    val post = Post(
      caption = editTextCaption.text.toString().trim(),
      //location = editTextLocation.text.toString().trim(),
      imageUrl = imageUrl,
      userId = userId,
      username = username,
      userProfileImageUrl = userProfileImageUrl,
      // timestamp se llena automáticamente gracias a @ServerTimestamp en el data class Post
    )

    firestore.collection("posts")
      .add(post)
      .addOnSuccessListener {
        Toast.makeText(this, "Post publicado con éxito!", Toast.LENGTH_LONG).show()
        // Cierra la actividad y vuelve a la anterior (probablemente el feed)
        finish()
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Error al publicar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
        resetShareButton()
      }
  }

  private fun resetShareButton() {
    buttonShare.isEnabled = true
    buttonShare.text = "Compartir"
  }
}
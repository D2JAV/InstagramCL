package com.example.instagramcl

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class UserProfileActivity : AppCompatActivity(), PostAdapter.OnPostClickListener {

  private lateinit var imageViewProfile: ImageView
  private lateinit var textViewUsername: TextView
  private lateinit var buttonFollow: Button
  private lateinit var buttonBack: ImageButton
  private lateinit var recyclerViewUserPosts: RecyclerView
  private lateinit var textViewNoPosts: TextView

  private lateinit var postAdapter: PostAdapter // Reutiliza tu adaptador de posts
  private var postList: MutableList<Post> = mutableListOf()

  private val auth = FirebaseAuth.getInstance()
  private val db = FirebaseFirestore.getInstance()

  // El ID del usuario cuyo perfil estamos viendo
  private var targetUserId: String? = null
  // El ID del usuario logeado
  private var currentUserId: String? = auth.currentUser?.uid


  override fun onPostClick(postId: String) {
    val intent = Intent(this, PostDetailActivity::class.java).apply {
      putExtra("POST_ID", postId)
    }
    startActivity(intent)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_user_profile)

    targetUserId = intent.getStringExtra("USER_ID")

    if (targetUserId.isNullOrEmpty()) {
      Toast.makeText(this, "Error: ID de usuario de destino inválido.", Toast.LENGTH_SHORT).show()
      // Log para saber dónde falló
      Log.e("USER_PROFILE_DEBUG",   "FATAL: targetUserId es nulo o vacío. Revisar UserAdapter/SearchActivity.")
      finish()
      return
    }
    currentUserId = auth.currentUser?.uid

    Log.d("USER_PROFILE_DEBUG", "Target User ID (Perfil a ver): $targetUserId")
    Log.d("USER_PROFILE_DEBUG", "Current User ID (Logueado): $currentUserId")


    // 1. Inicializar Vistas
    imageViewProfile = findViewById(R.id.imageViewProfile)
    textViewUsername = findViewById(R.id.textViewUsername)
    buttonFollow = findViewById(R.id.buttonFollow)
    buttonBack = findViewById(R.id.buttonBack)
    recyclerViewUserPosts = findViewById(R.id.recyclerViewUserPosts)
    textViewNoPosts = findViewById(R.id.textViewNoPosts)

    // 2. Configurar RecyclerView
    recyclerViewUserPosts.layoutManager = LinearLayoutManager(this)
    postAdapter = PostAdapter(this, postList ,this) // Asumiendo que PostAdapter toma Context y List<Post>
    recyclerViewUserPosts.adapter = postAdapter

    // 3. Cargar Datos y Listeners
    buttonBack.setOnClickListener { finish() }

    if (targetUserId == currentUserId) {
      // Es el perfil propio: ocultar botón de seguir y quizás mostrar botón de editar
      buttonFollow.visibility = View.GONE
    } else {
      // Perfil de otro usuario: configurar el botón de seguir
      loadFollowStatus()
      buttonFollow.setOnClickListener { toggleFollow() }
    }

    loadUserProfile()
    loadUserPosts()
  }

  private fun loadUserProfile() {
    db.collection("users").document(targetUserId!!)
      .get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val user = document.toObject(User::class.java)
          textViewUsername.text = user?.username

          Glide.with(this)
            .load(user?.profileImageUrl)
            .placeholder(R.drawable.image_placeholder)
            .circleCrop()
            .into(imageViewProfile)
        } else {
          Toast.makeText(this, "Usuario no encontrado.", Toast.LENGTH_SHORT).show()
        }
      }
      .addOnFailureListener { e ->
        Log.e("UserProfile", "Error loading user profile", e)
        Toast.makeText(this, "Error al cargar perfil.", Toast.LENGTH_SHORT).show()
      }
  }

  private fun loadFollowStatus() {
    if (currentUserId == null || targetUserId == null) return

    // 1. Comprobar si el usuario logeado ya sigue al usuario objetivo
    db.collection("following")
      .document(currentUserId!!)
      .collection("userFollowing")
      .document(targetUserId!!)
      .get()
      .addOnSuccessListener { document ->
        // Si el documento existe, significa que el usuario logeado YA lo sigue (DEJAR DE SEGUIR)
        if (document.exists()) {
          buttonFollow.text = "Dejar de Seguir"
          // **CAMBIO AQUI: Fondo Blanco, Letras Negras**
          buttonFollow.setBackgroundColor(Color.WHITE)
          buttonFollow.setTextColor(Color.BLACK)
        } else {
          // Si NO existe, significa que el usuario logeado NO lo sigue (SEGUIR)
          buttonFollow.text = "Seguir"
          // **CAMBIO AQUI: Fondo Negro, Letras Blancas**
          buttonFollow.setBackgroundColor(Color.BLACK)
          buttonFollow.setTextColor(Color.WHITE)
        }
      }
  }

  private fun toggleFollow() {
    if (currentUserId == null || targetUserId == null) return

    val currentFollowText = buttonFollow.text.toString()

    if (currentFollowText == "Seguir") {
      // Acción: Seguir
      followUser()
    } else {
      // Acción: Dejar de Seguir
      unfollowUser()
    }
  }

  private fun followUser() {
    // Añadir el usuario objetivo a la lista de 'userFollowing' del usuario logeado
    val followingRef = db.collection("following").document(currentUserId!!).collection("userFollowing").document(targetUserId!!)
    followingRef.set(mapOf("timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
      .addOnSuccessListener {
        // Añadir el usuario logeado a la lista de 'userFollowers' del usuario objetivo
        val followerRef = db.collection("followers").document(targetUserId!!).collection("userFollowers").document(
          currentUserId!!
        )
        followerRef.set(mapOf("timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()))

        Toast.makeText(this, "¡Siguiendo a ${textViewUsername.text}!", Toast.LENGTH_SHORT).show()
        loadFollowStatus() // Actualiza el botón
      }
  }

  private fun unfollowUser() {
    // Eliminar el usuario objetivo de la lista de 'userFollowing' del usuario logeado
    val followingRef = db.collection("following").document(currentUserId!!).collection("userFollowing").document(targetUserId!!)
    followingRef.delete()
      .addOnSuccessListener {
        // Eliminar el usuario logeado de la lista de 'userFollowers' del usuario objetivo
        val followerRef = db.collection("followers").document(targetUserId!!).collection("userFollowers").document(
          currentUserId!!
        )
        followerRef.delete()

        Toast.makeText(this, "Dejaste de seguir a ${textViewUsername.text}.", Toast.LENGTH_SHORT).show()
        loadFollowStatus() // Actualiza el botón
      }
  }

  private fun loadUserPosts() {
    db.collection("posts")
      .whereEqualTo("userId", targetUserId) // Filtra solo los posts de este usuario
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshots, e ->
        if (e != null) {
          Log.w("UserProfile", "Error al cargar posts.", e)
          return@addSnapshotListener
        }

        postList.clear()
        if (snapshots != null) {
          for (document in snapshots.documents) {
            val post = document.toObject(Post::class.java)?.apply {
              // ¡CRUCIAL! Asegúrate de que el ID del documento esté aquí
              postId = document.id
            }
            if (post != null) {
              postList.add(post)
            }
          }
        }

        postAdapter.notifyDataSetChanged()

        // Mostrar mensaje de "no posts" si la lista está vacía
        if (postList.isEmpty()) {
          textViewNoPosts.visibility = View.VISIBLE
          recyclerViewUserPosts.visibility = View.GONE
        } else {
          textViewNoPosts.visibility = View.GONE
          recyclerViewUserPosts.visibility = View.VISIBLE
        }
      }
  }
}
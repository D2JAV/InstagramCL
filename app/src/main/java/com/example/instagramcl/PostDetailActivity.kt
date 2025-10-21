package com.example.instagramcl

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
class PostDetailActivity : AppCompatActivity() {

  private lateinit var db: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private var postId: String? = null

  // Vistas de la UI del post
  private lateinit var ivDetailProfileImage: ImageView
  private lateinit var tvDetailUsername: TextView
  private lateinit var ivDetailPostImage: ImageView
  private lateinit var tvDetailDescription: TextView
  private lateinit var tvDetailTimestamp: TextView

  private lateinit var rvComments: RecyclerView
  private lateinit var commentAdapter: CommentAdapter
  private val commentList = mutableListOf<CommentWithUser>()
  // --- VISTAS NUEVAS PARA LA BARRA DE COMENTARIOS ---
  private lateinit var etCommentInput: EditText
  private lateinit var btnPostComment: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_post_detail)

    db = FirebaseFirestore.getInstance()
    auth = FirebaseAuth.getInstance()

    postId = intent.getStringExtra("POST_ID")

    if (postId == null) {
      Log.e("PostDetailActivity", "No se recibió el POST_ID.")
      Toast.makeText(this, "Error: No se pudo cargar el post.", Toast.LENGTH_LONG).show()
      finish()
      return
    }

    // --- INICIALIZACIÓN DEL RECYCLERVIEW Y ADAPTADOR (LA PARTE QUE FALTABA) ---
    rvComments = findViewById(R.id.rvComments)
    rvComments.layoutManager = LinearLayoutManager(this)
    commentAdapter = CommentAdapter(this, commentList) // <-- INICIALIZACIÓN CLAVE
    rvComments.adapter = commentAdapter

    // Inicializar las vistas del post
    ivDetailProfileImage = findViewById(R.id.ivDetailProfileImage)
    tvDetailUsername = findViewById(R.id.tvDetailUsername)
    ivDetailPostImage = findViewById(R.id.ivDetailPostImage)
    tvDetailDescription = findViewById(R.id.tvDetailDescription)
    tvDetailTimestamp = findViewById(R.id.tvDetailTimestamp)

    // --- INICIALIZAR VISTAS DE LA BARRA DE COMENTARIOS ---
    etCommentInput = findViewById(R.id.etCommentInput)
    btnPostComment = findViewById(R.id.btnPostComment)

    // Cargar los datos del post
    loadPostDetails()
    loadComments()
    // --- CONFIGURAR EL LISTENER DEL BOTÓN PUBLICAR ---
    btnPostComment.setOnClickListener {
      val commentText = etCommentInput.text.toString().trim()
      if (commentText.isNotEmpty()) {
        postComment(commentText)
      } else {
        Toast.makeText(this, "El comentario no puede estar vacío.", Toast.LENGTH_SHORT).show()
      }
    }
  }
  private fun loadComments() {
    if (postId == null) return

    db.collection("posts").document(postId!!)
      .collection("comments")
      .orderBy("timestamp", Query.Direction.ASCENDING)
      .addSnapshotListener { snapshots, e ->
        if (e != null) {
          Log.w("PostDetailActivity", "Error al escuchar comentarios.", e)
          return@addSnapshotListener
        }

        val comments = snapshots?.toObjects(Comment::class.java) ?: emptyList()
        val userIds = comments.map { it.userId }.distinct()

        if (userIds.isEmpty()) {
          commentList.clear()
          commentAdapter.notifyDataSetChanged()
          return@addSnapshotListener
        }

        db.collection("users").whereIn("uid", userIds).get()
          .addOnSuccessListener { userDocs ->
            val userMap = userDocs.toObjects(User::class.java).associateBy { it.uid }

            val newCommentList = comments.mapNotNull { comment ->
              userMap[comment.userId]?.let { user ->
                CommentWithUser(comment, user)
              }
            }

            commentList.clear()
            commentList.addAll(newCommentList)
            commentAdapter.notifyDataSetChanged()
          }
      }
  }

  private fun postComment(text: String) {
    val currentUserId = auth.currentUser?.uid
    if (currentUserId == null) {
      Toast.makeText(this, "Debes iniciar sesión para comentar.", Toast.LENGTH_SHORT).show()
      return
    }

    // Deshabilitar el botón para evitar envíos duplicados
    btnPostComment.isEnabled = false

    // La ruta será: posts/{postId}/comments/{commentId}
    val commentsCollection = db.collection("posts").document(postId!!).collection("comments")
    val commentId = commentsCollection.document().id // Generar un ID único para el comentario

    val newComment = Comment(
      commentId = commentId,
      postId = postId!!,
      userId = currentUserId,
      text = text
      // El timestamp se añade automáticamente por el servidor
    )

    commentsCollection.document(commentId).set(newComment)
      .addOnSuccessListener {
        Toast.makeText(this, "Comentario publicado.", Toast.LENGTH_SHORT).show()
        etCommentInput.text.clear() // Limpiar el campo de texto
        // Aquí podrías agregar lógica para refrescar la lista de comentarios
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Error al publicar: ${e.message}", Toast.LENGTH_SHORT).show()
      }
      .addOnCompleteListener {
        // Volver a habilitar el botón
        btnPostComment.isEnabled = true
      }
  }


  private fun loadPostDetails() {
    val postRef = db.collection("posts").document(postId!!)

    postRef.get().addOnSuccessListener { documentSnapshot ->
      if (documentSnapshot != null && documentSnapshot.exists()) {
        // En tu clase Post, asegúrate que el campo se llame 'description' o 'caption'
        val post = documentSnapshot.toObject(Post::class.java)
        if (post != null) {
          tvDetailDescription.text = post.caption // O post.caption
          Glide.with(this).load(post.imageUrl).into(ivDetailPostImage)

          post.timestamp?.let { date ->
            val formatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            tvDetailTimestamp.text = formatter.format(date)
          }
          loadUserDetails(post.userId)
        }
      } else {
        Log.d("PostDetailActivity", "El post no existe.")
        Toast.makeText(this, "El post ha sido eliminado.", Toast.LENGTH_SHORT).show()
      }
    }.addOnFailureListener { e ->
      Log.e("PostDetailActivity", "Error al obtener el post", e)
      Toast.makeText(this, "Error al cargar el post: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  private fun loadUserDetails(userId: String) {
    if (userId.isEmpty()) return
    val userRef = db.collection("users").document(userId)
    userRef.get().addOnSuccessListener { document ->
      if (document != null && document.exists()) {
        val username = document.getString("username")
        val profileImageUrl = document.getString("profileImageUrl")
        tvDetailUsername.text = username ?: "Usuario"
        if (!profileImageUrl.isNullOrEmpty()) {
          Glide.with(this).load(profileImageUrl).circleCrop().into(ivDetailProfileImage)
        } else {
          ivDetailProfileImage.setImageResource(R.drawable.image_placeholder)
        }
      }
    }.addOnFailureListener { e ->
      Log.e("PostDetailActivity", "Error al cargar datos del usuario", e)
    }
  }
}

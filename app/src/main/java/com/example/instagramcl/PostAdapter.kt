// PostAdapter.kt
package com.example.instagramcl

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
  private val context: Context,
  private val postList: List<Post>,
  private val listener: OnPostClickListener
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

  interface OnPostClickListener {
    fun onPostClick(postId: String)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
    val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
    return PostViewHolder(view)
  }

  override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
    val post = postList[position]
    holder.bind(post)
  }

  override fun getItemCount(): Int = postList.size

  inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // Referencias a las vistas del layout item_post.xml
    private val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
    private val tvPostDescription: TextView = itemView.findViewById(R.id.tvPostCaption)
    private val tvUsername: TextView = itemView.findViewById(R.id.tvUsernamePost)
    private val ivProfileImage: ImageView = itemView.findViewById(R.id.ivUserProfilePost)

    // --- VISTAS NUEVAS PARA LIKES ---
    private val ivLikeButton: ImageView = itemView.findViewById(R.id.ivLikeButton)
    private val tvLikesCount: TextView = itemView.findViewById(R.id.tvLikesCount)

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    init {
      // Listener para abrir el detalle del post
      itemView.setOnClickListener {
        val position = adapterPosition
        if (position != RecyclerView.NO_POSITION) {
          listener.onPostClick(postList[position].postId)
        }
      }

      // --- LISTENER PARA EL BOTÓN DE LIKE ---
      ivLikeButton.setOnClickListener {
        val position = adapterPosition
        if (position != RecyclerView.NO_POSITION && currentUserId != null) {
          handleLike(postList[position], currentUserId)
        }
      }
    }

    fun bind(post: Post) {
      tvUsername.text = "Cargando..." // Placeholder mientras carga
      tvPostDescription.text = post.caption
      Glide.with(context).load(post.imageUrl).into(ivPostImage)

      // --- LÓGICA PARA MOSTRAR LIKES E ÍCONO ---
      updateLikeStatus(post, currentUserId)

      // Cargar datos del usuario
      loadUserData(post.userId)
    }

    private fun updateLikeStatus(post: Post, userId: String?) {
      // Actualizar contador de likes
      tvLikesCount.text = "${post.likedBy.size} Me gusta"

      // Cambiar el ícono del botón si el usuario actual ha dado like
      if (userId != null && post.likedBy.contains(userId)) {
        ivLikeButton.setImageResource(R.drawable.like_red)
      } else {
        ivLikeButton.setImageResource(R.drawable.like_placeholder)
      }
    }

    private fun handleLike(post: Post, userId: String) {
      val postRef = db.collection("posts").document(post.postId)
      val isLiked = post.likedBy.contains(userId)

      if (isLiked) {
        // --- QUITAR LIKE ---
        // Quitar el UID del array en Firestore
        postRef.update("likedBy", FieldValue.arrayRemove(userId))
        // Actualizar la lista localmente para reflejar el cambio al instante
        post.likedBy.remove(userId)
      } else {
        // --- DAR LIKE ---
        // Añadir el UID al array en Firestore
        postRef.update("likedBy", FieldValue.arrayUnion(userId))
        // Actualizar la lista localmente
        post.likedBy.add(userId)
      }

      // Actualizar la UI inmediatamente
      updateLikeStatus(post, userId)
    }

    private fun loadUserData(userId: String) {
      if (userId.isEmpty()) return
      db.collection("users").document(userId).get().addOnSuccessListener { document ->
        if (document != null && document.exists()) {
          val username = document.getString("username") ?: "Usuario"
          val profileImageUrl = document.getString("profileImageUrl")

          tvUsername.text = username

          if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(context).load(profileImageUrl).circleCrop().into(ivProfileImage)
          } else {
            ivProfileImage.setImageResource(R.drawable.image_placeholder) // Un placeholder por defecto
          }
        }
      }.addOnFailureListener {
        tvUsername.text = "Error"
      }
    }
  }
}

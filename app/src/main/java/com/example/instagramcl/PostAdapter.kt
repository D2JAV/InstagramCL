package com.example.instagramcl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostAdapter(private val context: Context, private var postList: List<Post>) :
  RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
    val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
    return PostViewHolder(view)
  }

  override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
    val post = postList[position]
    holder.bind(post)
  }

  override fun getItemCount(): Int {
    return postList.size
  }

  fun updatePosts(newPosts: List<Post>) {
    postList = newPosts
    notifyDataSetChanged() // Forma simple de actualizar, considera DiffUtil para mejor rendimiento
  }

  inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val userProfileImageView: ImageView = itemView.findViewById(R.id.ivUserProfilePost)
    private val usernameTextView: TextView = itemView.findViewById(R.id.tvUsernamePost)
    private val postImageView: ImageView = itemView.findViewById(R.id.ivPostImage)
    private val likesTextView: TextView = itemView.findViewById(R.id.tvLikesCount)
    private val captionTextView: TextView = itemView.findViewById(R.id.tvPostCaption)
    // private val likeButton: ImageView = itemView.findViewById(R.id.ivLikeButton)
    // ... otros botones

    fun bind(post: Post) {
      usernameTextView.text = post.username
      captionTextView.text = post.caption
      likesTextView.text = "${post.likes} Me gusta"

      // Cargar imagen de perfil del usuario con Glide
      if (post.userProfileImageUrl.isNotEmpty()) {
        Glide.with(context)
          .load(post.userProfileImageUrl)
          .placeholder(R.drawable.ic_profile_placeholder) // Un placeholder genérico
          .error(R.drawable.ic_profile_placeholder) // Imagen de error
          .circleCrop() // Para hacerla circular
          .into(userProfileImageView)
      } else {
        userProfileImageView.setImageResource(R.drawable.ic_profile_placeholder)
      }

      if (post.imageUrl.isNotEmpty()) {
        Glide.with(context)
          .load(post.imageUrl)
          .placeholder(R.drawable.image_placeholder) // Correcto: usa un ID de drawable
          .error(R.drawable.image_placeholder)       // Correcto: usa un ID de drawable
          .into(postImageView)
      } else {
        // Ocultar o mostrar un placeholder si no hay imagen
        postImageView.setImageResource(R.drawable.image_placeholder)
      }

      // TODO: Implementar listeners para los botones (like, comment, etc.)
    }
  }
}


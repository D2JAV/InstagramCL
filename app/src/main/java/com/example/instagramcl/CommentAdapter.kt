package com.example.instagramcl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

data class CommentWithUser(val comment: Comment, val user: User)

class CommentAdapter(
  private val context: Context,
  private val commentList: List<CommentWithUser>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
    val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
    return CommentViewHolder(view)
  }

  override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
    val commentWithUser = commentList[position]
    holder.bind(commentWithUser)
  }

  override fun getItemCount(): Int = commentList.size

  inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val ivProfile: ImageView = itemView.findViewById(R.id.ivCommentUserProfile)
    private val tvUsername: TextView = itemView.findViewById(R.id.tvCommentUsername)
    private val tvComment: TextView = itemView.findViewById(R.id.tvCommentText)

    fun bind(commentWithUser: CommentWithUser) {
      tvUsername.text = commentWithUser.user.username
      tvComment.text = commentWithUser.comment.text

      // Cargar imagen de perfil del autor del comentario
      if (commentWithUser.user.profileImageUrl.isNotEmpty()) {
        Glide.with(context)
          .load(commentWithUser.user.profileImageUrl)
          .circleCrop()
          .into(ivProfile)
      } else {
        ivProfile.setImageResource(R.drawable.image_placeholder) // Imagen por defecto
      }
    }
  }
}

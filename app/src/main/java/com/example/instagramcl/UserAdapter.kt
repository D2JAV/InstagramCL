
package com.example.instagramcl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Intent
import android.util.Log

class UserAdapter(
  private val context: Context,
  private val userList: List<User>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

  class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val username: TextView = itemView.findViewById(R.id.textViewUsername)
    val profileImage: ImageView = itemView.findViewById(R.id.imageViewProfile)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
    val view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false)
    return UserViewHolder(view)
  }

  override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
    val user = userList[position]
    holder.username.text = user.username

    // Cargar imagen de perfil (usa un placeholder si la URL está vacía o nula)
    Glide.with(context)
      .load(user.profileImageUrl)
      .placeholder(R.drawable.image_placeholder)
      .circleCrop()
      .into(holder.profileImage)

    // Manejar el clic para ir al perfil del usuario
    holder.itemView.setOnClickListener {

      val intent = Intent(context, UserProfileActivity::class.java).apply {
        // Pasar el ID del usuario seleccionado

        if (user.uid.isEmpty()){

          Log.e("USER_Adapter",   "FATAL: targetUserId es nulo o vacío. Revisar UserAdapter ")
          return@setOnClickListener
        }
        putExtra("USER_ID", user.uid)
      }
      context.startActivity(intent)
    }
  }

  override fun getItemCount(): Int = userList.size
}
package com.example.instagramcl

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
  var postId: String = "", // ID único del post, usualmente el ID del documento en Firestore
  val userId: String = "", // UID del usuario que creó el post
  var username: String = "", // Nombre de usuario del autor (puede denormalizarse aquí)
  var userProfileImageUrl: String = "", // URL de la imagen de perfil del autor (denormalizado)
  val imageUrl: String = "", // URL de la imagen o video del post
  val caption: String = "",
  @ServerTimestamp // Esto se llenará automáticamente con la marca de tiempo del servidor
  val timestamp: Date? = null,
  val likedBy: MutableList<String> = mutableListOf()
  // crear la lista de usuarios que dieron like
  // val likedBy: List<String> = emptyList()
  // crear el contador de comentarios
  // var commentsCount: Long = 0
) {
  // Constructor vacío requerido por Firestore para la deserialización
  constructor() : this("", "", "", "", "", "", null, mutableListOf())
}



package com.example.instagramcl

data class User(
  val email: String = "",
  val profileImageUrl: String = "",
  var uid: String = "",
  var username: String? = null,
  // Puedes añadir más campos como 'bio', 'fullName', etc.
) {
  // Constructor vacío requerido por Firestore
  constructor() : this("", "", "", "")
}
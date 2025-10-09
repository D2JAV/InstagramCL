
package com.example.instagramcl

data class User(
  var uid: String = "",
  var username: String? = null,
  val email: String = "",
  val profileImageUrl: String = ""
  // Puedes añadir más campos como 'bio', 'fullName', etc.
) {
  // Constructor vacío requerido por Firestore
  constructor() : this("", "", "", "")
}
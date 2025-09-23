package com.example.instagramcl

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu // Para el menú de la Toolbar
import android.view.MenuItem // Para el menú de la Toolbar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // Para ordenar los posts

class FeedActivity : AppCompatActivity() {

  private lateinit var auth: FirebaseAuth
  private lateinit var db: FirebaseFirestore
  private lateinit var recyclerViewFeed: RecyclerView
  private lateinit var postAdapter: PostAdapter
  private var postList: MutableList<Post> = mutableListOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_feed)

    auth = FirebaseAuth.getInstance()
    db = FirebaseFirestore.getInstance()

    val toolbar: Toolbar = findViewById(R.id.toolbarFeed)
    setSupportActionBar(toolbar)
    // Quitar el título por defecto si lo pones en el XML o quieres un logo
    // supportActionBar?.setDisplayShowTitleEnabled(false)


    recyclerViewFeed = findViewById(R.id.recyclerViewFeed)
    recyclerViewFeed.layoutManager = LinearLayoutManager(this)
    postAdapter = PostAdapter(this, postList)
    recyclerViewFeed.adapter = postAdapter

    //agregar esta opcion a una barra horizontal en la parte baja del feed
//    val fabCreatePost: FloatingActionButton = findViewById(R.id.fabCreatePost)
//    fabCreatePost.setOnClickListener {
//      // TODO: Navegar a la pantalla de creación de posts
//      Toast.makeText(this, "Navegar a crear post", Toast.LENGTH_SHORT).show()
//    }

    // Verificar si el usuario está logueado
    if (auth.currentUser == null) {
      // Si no está logueado, volver a LoginActivity
      goToLoginActivity()
      return // Importante para no continuar con la carga de datos
    }

    fetchPosts()
  }

  private fun fetchPosts() {
    // Obtener posts ordenados por fecha descendente (los más nuevos primero)
    db.collection("posts")
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshots, e -> // addSnapshotListener para tiempo real
        if (e != null) {
          Log.w("FeedActivity", "Listen failed.", e)
          Toast.makeText(this, "Error al cargar posts: ${e.message}", Toast.LENGTH_SHORT).show()
          return@addSnapshotListener
        }

        if (snapshots != null) {
          val newPosts = mutableListOf<Post>()
          for (document in snapshots.documents) {
            val post = document.toObject(Post::class.java)
            if (post != null) {
              // Opcional: Si tienes el ID del documento como campo en el objeto Post
              // post.postId = document.id
              newPosts.add(post)
            }
          }
          postList.clear()
          postList.addAll(newPosts)
          postAdapter.notifyDataSetChanged() // Actualiza el adaptador
          // O, para una mejor experiencia si usas DiffUtil en el adapter:
          // postAdapter.updatePosts(newPosts)
          Log.d("FeedActivity", "Posts cargados: ${postList.size}")
        } else {
          Log.d("FeedActivity", "Current data: null")
        }
      }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_feed, menu) // Crea res/menu/menu_feed.xml
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_logout -> {
        auth.signOut()
        goToLoginActivity()
        true
      }
      R.id.action_profile -> {
        // TODO: Navegar a la pantalla de perfil
        Toast.makeText(this, "Navegar a Perfil", Toast.LENGTH_SHORT).show()
        true
      }
      // ... otros items del menú
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun goToLoginActivity() {
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
  }
}


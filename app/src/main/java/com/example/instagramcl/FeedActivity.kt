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
import com.google.android.material.bottomnavigation.BottomNavigationView
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

  // Mantenemos la referencia al FAB por si lo dejas en el XML
  private lateinit var bottomNavigationView: BottomNavigationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // 🌟 Asegúrate de que el tema de esta Activity en AndroidManifest.xml sea NoActionBar
    setContentView(R.layout.activity_feed)

    auth = FirebaseAuth.getInstance()
    db = FirebaseFirestore.getInstance()

    // ❌ Eliminamos toda inicialización y referencia a la Toolbar

    recyclerViewFeed = findViewById(R.id.recyclerViewFeed)
    recyclerViewFeed.layoutManager = LinearLayoutManager(this)
    postAdapter = PostAdapter(this, postList)
    recyclerViewFeed.adapter = postAdapter

//    // Inicializar el FloatingActionButton (asumo que lo mantienes en el layout)
//    fabCreatePost = findViewById(R.id.fabCreatePost)

    // Si tu FAB es solo un placeholder, puedes incluso remover este listener
    // y el FAB del XML si la acción de crear post va en la BottomNavigationView

    // Inicializar la BottomNavigationView
    bottomNavigationView = findViewById(R.id.bottomNavigationView)
    setupBottomNavListener()

    // Verificar si el usuario está logueado
    if (auth.currentUser == null) {
      goToLoginActivity()
      return
    }

    fetchPosts()
  }

  /**
   * Configura el listener para los clics en la barra de navegación inferior.
   */
  private fun setupBottomNavListener() {
    bottomNavigationView.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.nav_home -> {
          // Ya estás en Home (FeedActivity)
          true
        }
        R.id.nav_search -> {
          val intent = Intent(this, SearchActivity::class.java)
          startActivity(intent)
          true
        }
        R.id.nav_newPost -> {
          // Asumiendo que el ícono central es el de crear post (o reels, según lo configures)
          goToCreatePostActivity() // Mueve la acción del FAB a este ícono
          true
        }
        R.id.nav_reels -> {
        // Asumiendo que el ícono central es el de crear post (o reels, según lo configures)
          Toast.makeText(this, "Ir a reels", Toast.LENGTH_SHORT).show()
          true
       }
        R.id.nav_profile -> {
          Toast.makeText(this, "Ir a Perfil", Toast.LENGTH_SHORT).show()
          true
        }
        // Agrega aquí el ID del ícono de Tienda/Shop (si lo incluiste)
        else -> false
      }
    }
  }

  private fun goToCreatePostActivity() {
    val intent = Intent(this, CreatePostActivity::class.java)
    startActivity(intent)
  }

  private fun fetchPosts() {
    // Tu lógica de fetching de posts sigue igual
    db.collection("posts")
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshots, e ->
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
              newPosts.add(post)
            }
          }
          postList.clear()
          postList.addAll(newPosts)
          postAdapter.notifyDataSetChanged()
          Log.d("FeedActivity", "Posts cargados: ${postList.size}")
        } else {
          Log.d("FeedActivity", "Current data: null")
        }
      }
  }

  // ❌ Eliminamos override fun onCreateOptionsMenu (relacionado con el antiguo Toolbar)
  // ❌ Eliminamos override fun onOptionsItemSelected (relacionado con el antiguo Toolbar)

  private fun goToLoginActivity() {
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
  }
}
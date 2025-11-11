package com.example.instagramcl

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedActivity : AppCompatActivity(), PostAdapter.OnPostClickListener {

  private lateinit var auth: FirebaseAuth
  private lateinit var db: FirebaseFirestore
  private lateinit var recyclerViewFeed: RecyclerView
  private lateinit var postAdapter: PostAdapter
  private var postList: MutableList<Post> = mutableListOf()

  private lateinit var bottomNavigationView: BottomNavigationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_feed)

    auth = FirebaseAuth.getInstance()
    db = FirebaseFirestore.getInstance()

    recyclerViewFeed = findViewById(R.id.recyclerViewFeed)
    recyclerViewFeed.layoutManager = LinearLayoutManager(this)

    // El listener 'this' se pasa al adaptador para que sepa a quién notificar
    postAdapter = PostAdapter(this, postList, this)
    recyclerViewFeed.adapter = postAdapter

    bottomNavigationView = findViewById(R.id.bottomNavigationView)
    setupBottomNavListener()

    if (auth.currentUser == null) {
      goToLoginActivity()
      return
    }

    fetchPosts()
  }

  //cuando se da click en un post
  override fun onPostClick(postId: String) {
    // Iniciar la actividad de detalle y pasar el ID del post
    val intent = Intent(this, PostDetailActivity::class.java).apply {
      putExtra("POST_ID", postId)
    }
    startActivity(intent)
  }

  private fun fetchPosts() {
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
            val post = document.toObject(Post::class.java)?.apply {
              // ¡CRUCIAL! Asignar el ID del documento al objeto Post
              postId = document.id
            }
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

  // --- Otros métodos de la clase (setupBottomNavListener, goToCreatePostActivity, etc.) ---

  private fun setupBottomNavListener() {
    bottomNavigationView.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.nav_home -> {
          recyclerViewFeed.smoothScrollToPosition(0)
          true
        }
        R.id.nav_search -> {
          val intent = Intent(this, SearchActivity::class.java)
          startActivity(intent)
          true
        }
        R.id.nav_newPost -> {
          goToCreatePostActivity()
          true
        }

        R.id.nav_profile -> {
          val currentUserId = auth.currentUser?.uid
          if (currentUserId != null) {
            val intent = Intent(this, UserProfileActivity::class.java).apply {
              putExtra("USER_ID", currentUserId)
            }
            startActivity(intent)
          }
          true
        }
        else -> false
      }
    }
  }

  private fun goToCreatePostActivity() {
    val intent = Intent(this, CreatePostActivity::class.java)
    startActivity(intent)
  }

  private fun goToLoginActivity() {
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
  }
}
  // Archivo: SearchActivity.kt
  package com.example.instagramcl

  import android.content.Context
  import android.os.Bundle
  import android.view.inputmethod.EditorInfo
  import android.view.inputmethod.InputMethodManager
  import android.widget.Button
  import android.widget.EditText
  import android.widget.Toast
  import androidx.appcompat.app.AppCompatActivity
  import androidx.recyclerview.widget.LinearLayoutManager
  import androidx.recyclerview.widget.RecyclerView
  import com.google.firebase.firestore.FirebaseFirestore

  class SearchActivity : AppCompatActivity() {

    private lateinit var editTextSearch: EditText
    private lateinit var buttonCancel: Button
    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private var userList: MutableList<User> = mutableListOf()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_search)

      editTextSearch = findViewById(R.id.editTextSearch)
      buttonCancel = findViewById(R.id.buttonCancel)
      recyclerViewUsers = findViewById(R.id.recyclerViewUsers)

      setupRecyclerView()
      setupSearchInput()
      setupCancelButton()
    }

    private fun setupRecyclerView() {
      recyclerViewUsers.layoutManager = LinearLayoutManager(this)
      userAdapter = UserAdapter(this, userList)
      recyclerViewUsers.adapter = userAdapter
    }

    private fun setupSearchInput() {
      // Abrir el teclado automáticamente y enfocar el campo
      editTextSearch.post {
        //editTextSearch.requestFocus()
        showKeyboard()
      }

      // Configurar la acción de "Buscar" en el teclado
      editTextSearch.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          performSearch(editTextSearch.text.toString().trim())
          hideKeyboard()
          true
        } else {
          false
        }
      }
    }

    private fun setupCancelButton() {
      buttonCancel.setOnClickListener {
        // 1. Limpiar el campo de texto
        editTextSearch.setText("")
        // 2. Limpiar la lista de resultados
        userList.clear()
        userAdapter.notifyDataSetChanged()
        // 3. Opcional: Ocultar el teclado
        hideKeyboard()
      }
    }
    private fun performSearch(query: String) {
      if (query.isEmpty()) {
        userList.clear()
        userAdapter.notifyDataSetChanged()
        return
      }

      val searchEnd = query + "\uf8ff"

      db.collection("users")
        .whereGreaterThanOrEqualTo("username", query)
        .whereLessThan("username", searchEnd)
        .limit(50)
        .get()
        .addOnSuccessListener { result ->
          userList.clear()

          for (document in result) {
            val user = document.toObject(User::class.java)

            // 🔥 CORRECCIÓN CRÍTICA: Asigna el ID del documento
            if (user != null) {
              // 1. Asigna el ID del documento de Firestore al campo userId del objeto User.
              user.uid = document.id
              userList.add(user)
            }
          }

          userAdapter.notifyDataSetChanged()

          if (userList.isEmpty()) {
            Toast.makeText(this, "No se encontraron usuarios para '$query'", Toast.LENGTH_SHORT).show()
          }
        }
        .addOnFailureListener { e ->
          Toast.makeText(this, "Error de búsqueda: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showKeyboard() {
      val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      imm.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
      val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      imm.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
    }
  }
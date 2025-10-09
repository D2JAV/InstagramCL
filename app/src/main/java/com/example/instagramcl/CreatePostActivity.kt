package com.example.instagramcl

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.ContentValues
class CreatePostActivity : AppCompatActivity() {

    private lateinit var imageViewPreview: ImageView
    private lateinit var recyclerViewGallery: RecyclerView
    private lateinit var buttonNext: Button
    private lateinit var buttonClose: ImageButton

    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    // --- LANZADORES DE PERMISOS Y ACTIVIDADES ---

    // 1. Lanzador para solicitar permisos de LECTURA
    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) loadImagesFromGallery()
            else permissionDenied()
        }

    // 2. Lanzador para solicitar permisos de CÁMARA
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) launchCamera()
            else permissionDenied("Se requieren permisos de cámara para tomar una foto.")
        }

    // 3. Lanzador para la CÁMARA
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let {
                updatePreview(it) // Actualiza la vista previa con la foto tomada
            }
        }
    }

    // --- CICLO DE VIDA ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        imageViewPreview = findViewById(R.id.imageViewPreview)
        recyclerViewGallery = findViewById(R.id.recyclerViewGallery)
        buttonNext = findViewById(R.id.buttonNext)
        buttonClose = findViewById(R.id.buttonClose)

        buttonClose.setOnClickListener { finish() }
        buttonNext.setOnClickListener {
            // ************ MODIFICACIÓN AQUÍ ************
            selectedImageUri?.let { uri ->
                val intent = Intent(this, PublishPostActivity::class.java).apply {
                    // Se pasa la URI como String para que la reciba la siguiente Activity
                    putExtra("IMAGE_URI", uri.toString())
                }
                startActivity(intent)
            } ?: Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show()
        }

        // El flujo de permisos de almacenamiento inicia aquí
        requestStoragePermission()
    }

    // --- LÓGICA DE PERMISOS ---

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        handlePermission(permission, requestStoragePermissionLauncher)
    }

    private fun requestCameraPermission() {
        handlePermission(Manifest.permission.CAMERA, requestCameraPermissionLauncher)
    }

    private fun handlePermission(permission: String, launcher: androidx.activity.result.ActivityResultLauncher<String>) {
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Si el permiso ya fue dado, ejecutar la acción correspondiente
                if (permission == Manifest.permission.CAMERA) launchCamera() else loadImagesFromGallery()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                permissionDenied() // Muestra un mensaje explicando por qué se necesita
            }
            else -> {
                launcher.launch(permission) // Pide el permiso
            }
        }
    }

    private fun permissionDenied(message: String = "Se requieren permisos para acceder al contenido.") {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // --- LÓGICA DE CÁMARA Y GALERÍA ---

    private fun launchCamera() {
        // Crea una URI para guardar la imagen
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "new_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        cameraImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        // --- SOLUCIÓN ---
        // Asegúrate de que la URI no es nula antes de lanzar la cámara.
        cameraImageUri?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    private fun loadImagesFromGallery() {
        val imageList = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageList.add(contentUri)
            }
        }

        setupRecyclerView(imageList)

        if (imageList.isNotEmpty()) {
            updatePreview(imageList[0]) // Muestra la primera imagen por defecto
        } else {
            // Si no hay imágenes, deshabilita el botón "Siguiente"
            updateNextButtonState(false)
            // Aquí podrías mostrar una vista vacía o solo el botón de la cámara
        }
    }

    private fun setupRecyclerView(images: List<Uri>) {
        // Inicializa el adapter con las dos funciones lambda
        val galleryAdapter = GalleryAdapter(images,
            onImageClick = { imageUri ->
                updatePreview(imageUri)
            },
            onCameraClick = {
                requestCameraPermission() // Inicia el flujo de permisos de cámara
            }
        )
        recyclerViewGallery.layoutManager = GridLayoutManager(this, 4)
        recyclerViewGallery.adapter = galleryAdapter
    }


    // --- LÓGICA DE UI ---

    private fun updatePreview(uri: Uri) {
        selectedImageUri = uri
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(imageViewPreview)
        updateNextButtonState(true)
    }

    private fun updateNextButtonState(isEnabled: Boolean) {
        buttonNext.isEnabled = isEnabled
        buttonNext.alpha = if (isEnabled) 1.0f else 0.5f
    }
}

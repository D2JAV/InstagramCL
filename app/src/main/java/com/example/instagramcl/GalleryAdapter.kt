package com.example.instagramcl

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GalleryAdapter(
  private val images: List<Uri>,
  private val onImageClick: (Uri) -> Unit,
  private val onCameraClick: () -> Unit // Nuevo: Lambda para el clic en la cámara
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  // 1. Definir tipos de vista
  companion object {
    private const val VIEW_TYPE_CAMERA = 0
    private const val VIEW_TYPE_IMAGE = 1
  }

  // ViewHolder para la imagen
  class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

  // ViewHolder para el botón de la cámara
  class CameraViewHolder(view: View) : RecyclerView.ViewHolder(view)

  override fun getItemViewType(position: Int): Int {
    // El primer elemento siempre es la cámara
    return if (position == 0) VIEW_TYPE_CAMERA else VIEW_TYPE_IMAGE
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return if (viewType == VIEW_TYPE_CAMERA) {
      // Inflar el layout del botón de cámara
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_camera_button, parent, false)
      CameraViewHolder(view)
    } else {
      // Inflar el layout de la imagen de galería
      val imageView = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_gallery_image, parent, false) as ImageView
      ImageViewHolder(imageView)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    if (holder.itemViewType == VIEW_TYPE_CAMERA) {
      // Configurar el listener para el botón de cámara
      holder.itemView.setOnClickListener {
        onCameraClick()
      }
    } else {
      // La posición real en la lista de imágenes es `position - 1`
      val imageUri = images[position - 1]
      val imageViewHolder = holder as ImageViewHolder
      Glide.with(imageViewHolder.imageView.context)
        .load(imageUri)
        .centerCrop()
        .into(imageViewHolder.imageView)

      imageViewHolder.imageView.setOnClickListener {
        onImageClick(imageUri)
      }
    }
  }

  // El conteo total es el número de imágenes + 1 (para la cámara)
  override fun getItemCount(): Int = images.size + 1
}

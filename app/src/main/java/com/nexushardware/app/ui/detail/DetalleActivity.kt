package com.nexushardware.app.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nexushardware.app.data.local.NexusBDHelper
import com.nexushardware.app.databinding.ActivityDetalleBinding
import java.text.NumberFormat
import java.util.Locale

import com.google.android.material.snackbar.Snackbar
import android.graphics.Color

class DetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar para que tenga botón "Atrás"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // es para ocultar el titulo deafult

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Recibir datos del Intent
        val idProducto = intent.getIntExtra("id", -1)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val precio = intent.getDoubleExtra("precio", 0.0)
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val stock = intent.getIntExtra("stock", 0)
        val categoria = intent.getStringExtra("categoria") ?: ""
        val urlImagen = intent.getStringExtra("url") ?: ""


        //sirve para llenar la UI
        binding.tvNombreDetalle.text = nombre
        binding.tvDescripcionDetalle.text = descripcion
        binding.tvStock.text = "Stock: $stock unidades"
        binding.chipCategoria.text = categoria
        //carga la imagen con glide
        Glide.with(this)
            .load(urlImagen.takeIf { it.isNotEmpty() })
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .fallback(android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(binding.imgDetalle)


        val format = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        binding.tvPrecioDetalle.text = format.format(precio)

        //logica del boton agregar
        binding.fabAgregar.setOnClickListener {
            if (idProducto != -1) {
                val db = NexusBDHelper(this)

                // Por ahora usamos el usuario_id del admin
                val exito = db.agregarAlCarrito(usuarioId = 1, productoId = idProducto, cantidad = 1)

                if (exito > -1) {
                    //snackbar de exito
                    Snackbar.make(binding.root, "✅ Agregado al Carrito", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#03DAC5"))
                        .setTextColor(Color.BLACK)
                        .setActionTextColor(Color.BLACK) // El botón "OK" también en negro
                        .setAction("OK") {
                            // Se cierra automáticamente al hacer clic
                        }
                        .show()
                } else {
                    // snackbar de errores
                    Snackbar.make(binding.root, "❌ Error al agregar", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#CF6679"))
                        .setTextColor(Color.BLACK)
                        .show()
                }
            } else {
                Snackbar.make(binding.root, "Error: Producto no identificado", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#CF6679"))
                    .setTextColor(Color.BLACK)
                    .show()
            }
        }
    }
}
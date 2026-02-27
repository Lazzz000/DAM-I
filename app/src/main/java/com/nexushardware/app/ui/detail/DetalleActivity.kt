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

        // Construimos la url completa usando GLIDE (igual que en el Adapter)
        val urlBase = "http://127.0.0.1:8081/img/"
        val urlCompleta = urlBase + urlImagen

        //carga la imagen con glide usando la URL completa
        Glide.with(this)
            .load(urlCompleta) //aquí pasamos la ruta lista
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

                try {
                    //USO MIS VARIABLES EXACTAS PARA LA CACHÉ
                    db.agregarAlCarrito(
                        usuarioId = 1,
                        productoId = idProducto,
                        nombre = nombre,       // Tu variable
                        precio = precio,       // Tu variable
                        urlImagen = urlImagen, // Tu variable
                        stockNube = stock,     // Tu variable
                        cantidad = 1
                    )

                    //snackbar de exito
                    Snackbar.make(binding.root, "✅ Agregado al Carrito", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#03DAC5"))
                        .setTextColor(Color.BLACK)
                        .setActionTextColor(Color.BLACK)
                        .setAction("OK") { }
                        .show()

                } catch (e: NexusBDHelper.StockInsuficienteException) {
                    //aqui atrapamos el límite de stock
                    Snackbar.make(binding.root, "⚠️ ${e.message}", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#CF6679"))
                        .setTextColor(Color.BLACK)
                        .show()
                } catch (e: Exception) {
                    // snackbar de errores generales
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
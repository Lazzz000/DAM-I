package com.nexushardware.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nexushardware.app.databinding.ActivityDetalleBinding
import java.text.NumberFormat
import java.util.Locale

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
        val nombre = intent.getStringExtra("nombre") ?: ""
        val precio = intent.getDoubleExtra("precio", 0.0)
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val stock = intent.getIntExtra("stock", 0)
        val categoria = intent.getStringExtra("categoria") ?: ""

        //sirve para llenar la UI
        binding.tvNombreDetalle.text = nombre
        binding.tvDescripcionDetalle.text = descripcion
        binding.tvStock.text = "Stock: $stock unidades"
        binding.chipCategoria.text = categoria

        val format = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        binding.tvPrecioDetalle.text = format.format(precio)

        //acción del Botón Flotante
        binding.fabAgregar.setOnClickListener {
            Toast.makeText(this, "¡$nombre agregado al carrito!", Toast.LENGTH_LONG).show()
            // en adelante aqui guardaremos ta tabla carrito
        }
    }
}
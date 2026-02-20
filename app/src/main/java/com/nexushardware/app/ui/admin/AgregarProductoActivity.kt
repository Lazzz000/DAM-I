package com.nexushardware.app.ui.admin

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.nexushardware.app.databinding.ActivityAgregarProductoBinding
import com.nexushardware.app.data.local.NexusBDHelper

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarProductoBinding
    private lateinit var dbHelper: NexusBDHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = NexusBDHelper(this)

        binding.btnGuardarProducto.setOnClickListener {
            registrarNuevoProducto()
        }
    }

    private fun registrarNuevoProducto() {
        // primero se captura los datos
        val nombre = binding.etNombreProd.text.toString().trim()
        val categoria = binding.etCategoriaProd.text.toString().trim()
        val precioStr = binding.etPrecioProd.text.toString().trim()
        val stockStr = binding.etStockProd.text.toString().trim()
        val descripcion = binding.etDescProd.text.toString().trim()

        //validación
        if (nombre.isEmpty() || categoria.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Snackbar.make(binding.root, "⚠️ Completa todos los campos obligatorios", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.parseColor("#CF6679")) // Rojo de error
                .show()
            return
        }

        //convertir textos a numeros
        val precio = precioStr.toDoubleOrNull() ?: 0.0
        val stock = stockStr.toIntOrNull() ?: 0

        //genera URL dinámica para la img/reemplaza los espacios del nombre con "+" para la url
        val textoImagen = nombre.replace(" ", "+")
        val urlGenerada = "https://dummyimage.com/600x600/1f1f1f/03dac5.png&text=$textoImagen"

        //guardar en bd
        val idInsertado = dbHelper.agregarProducto(nombre, descripcion, precio, stock, categoria, urlGenerada)

        if (idInsertado > -1) {
            //exito en el guardado
            Snackbar.make(binding.root, "✅ Componente registrado con éxito", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.parseColor("#03DAC5"))
                .setTextColor(Color.BLACK)
                .show()

            //limpiar formulario
            binding.etNombreProd.text?.clear()
            binding.etCategoriaProd.text?.clear()
            binding.etPrecioProd.text?.clear()
            binding.etStockProd.text?.clear()
            binding.etDescProd.text?.clear()

            //devolver foco al primer campo
            binding.etNombreProd.requestFocus()

        } else {
            Snackbar.make(binding.root, "❌ Error al guardar en la base de datos", Snackbar.LENGTH_SHORT).show()
        }
    }
}
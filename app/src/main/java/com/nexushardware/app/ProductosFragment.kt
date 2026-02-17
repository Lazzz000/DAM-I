package com.nexushardware.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexushardware.app.databinding.FragmentProductosBinding

class ProductosFragment : Fragment() {

    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: NexusBDHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = NexusBDHelper(requireContext())
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val lista = obtenerProductosDeBD()

        val adapter = ProductoAdapter(lista) { producto ->
            // Creamos el Intent para abrir el detalle
            val intent = android.content.Intent(context, DetalleActivity::class.java).apply {
                putExtra("nombre", producto.nombre)
                putExtra("descripcion", producto.descripcion)
                putExtra("precio", producto.precio)
                putExtra("stock", producto.stock)
                putExtra("categoria", producto.categoria)
                // putExtra("url", producto.urlImagen)
            }
            startActivity(intent)
        }

        binding.rvProductos.layoutManager = LinearLayoutManager(context)
        binding.rvProductos.adapter = adapter
    }

    private fun obtenerProductosDeBD(): List<Producto> {
        val lista = mutableListOf<Producto>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM productos", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val descripcion = cursor.getString(2)
                val precio = cursor.getDouble(3)
                val stock = cursor.getInt(4)
                val categoria = cursor.getString(5)
                val url = cursor.getString(6) ?: ""

                lista.add(Producto(id, nombre, descripcion, precio, stock, categoria, url))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
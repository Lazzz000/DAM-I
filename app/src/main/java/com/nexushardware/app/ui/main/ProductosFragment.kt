package com.nexushardware.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexushardware.app.data.local.NexusBDHelper
import com.nexushardware.app.data.model.Producto
import com.nexushardware.app.data.network.RetrofitClient
import com.nexushardware.app.databinding.FragmentProductosBinding
import com.nexushardware.app.ui.detail.DetalleActivity
import com.nexushardware.app.utils.adapters.ProductoAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductosFragment : Fragment() {

    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!

    //mantenemos tu dbHelper por si luego lo necesitas para otras validaciones
    private lateinit var dbHelper: NexusBDHelper

    //variable global para guardar el catálogo original descargado en memoria RAM
    private var listaProductosNube: List<Producto> = emptyList()

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

        //Iniciamos la carga del RecyclerView y la llamada a la nube
        setupRecyclerView()

        //configuramos el buscador de la UI
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val textoBusqueda = newText ?: ""

                //filtramos la lista que está en memoria
                val listaFiltrada = listaProductosNube.filter { producto ->
                    producto.nombre.contains(textoBusqueda, ignoreCase = true) ||
                            producto.categoria.nombreCategoria.contains(textoBusqueda, ignoreCase = true)
                }

                //le pasamos la nueva lista filtrada a un nuevo Adapter
                val adapterFiltrado = ProductoAdapter(listaFiltrada) { productoClickeado ->
                    // Llamamos a nuestra función reutilizable
                    navegarAlDetalle(productoClickeado)
                }
                binding.rvProductos.adapter = adapterFiltrado

                return true
            }
        })
    }

    private fun setupRecyclerView() {
        binding.rvProductos.layoutManager = LinearLayoutManager(context)
        cargarProductosDesdeNube()
    }

    private fun cargarProductosDesdeNube() {
        //usamos lifecycleScope para no congelar la pantalla
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                //hacemos la llamada al backend de Spring Boot (TechNova)
                val response = RetrofitClient.apiService.obtenerProductos()

                if (response.isSuccessful) {
                    val listaProductos = response.body() ?: emptyList()
                    listaProductosNube = listaProductos // GUARDAMOS LA LISTA EN MEMORIA

                    //volvemos al hilo principal para actualizar la interfaz gráfica
                    withContext(Dispatchers.Main) {
                        val adapter = ProductoAdapter(listaProductos) { producto ->
                            // Llamamos a nuestra función reutilizable
                            navegarAlDetalle(producto)
                        }
                        binding.rvProductos.adapter = adapter
                    }
                } else {
                    Log.e("API_NEXUS", "Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API_NEXUS", "Error de conexión: ${e.message}")
                    // Aquí podrías mostrar un Snackbar de error "Dark Tech"
                }
            }
        }
    }

    // FUNCIÓN REUTILIZABLE para asi no reptir dos veces
    private fun navegarAlDetalle(producto: Producto) {
        val intent = Intent(context, DetalleActivity::class.java).apply {
            putExtra("id", producto.id)
            putExtra("nombre", producto.nombre)
            putExtra("descripcion", producto.descripcion)
            putExtra("precio", producto.precio)
            putExtra("stock", producto.stock)
            // Extraemos el String del objeto Categoria
            putExtra("categoria", producto.categoria.nombreCategoria)
            putExtra("url", producto.urlImagen)
        }
        startActivity(intent)
    }

    /*esta función servía para obtener el productto con la bd local
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
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.nexushardware.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexushardware.app.data.local.NexusBDHelper
import com.nexushardware.app.data.model.Producto
import com.nexushardware.app.databinding.FragmentProductosBinding
import com.nexushardware.app.ui.detail.DetalleActivity
import com.nexushardware.app.utils.adapters.ProductoAdapter

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

        //Logica de la barra de busqueda de prod
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                //Se ejecuta al presionar Enter en el teclado pero no lo usaremos porque filtraremos en vivo
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //se ejecuta cada vez que el usuario teclea o borra una letra
                if (newText != null) {
                    //buscamos en la bd
                    val listaFiltrada = dbHelper.buscarProductos(newText)

                    //actualizamos el RecyclerView usando la función que ya teníamos en el Adapter
                    val adapter = binding.rvProductos.adapter as ProductoAdapter
                    adapter.actualizarLista(listaFiltrada)
                }
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        val lista = obtenerProductosDeBD()

        val adapter = ProductoAdapter(lista) { producto ->
            // Creamos el Intent para abrir el detalle
            val intent = Intent(context, DetalleActivity::class.java).apply {
                putExtra("id", producto.id)
                putExtra("nombre", producto.nombre)
                putExtra("descripcion", producto.descripcion)
                putExtra("precio", producto.precio)
                putExtra("stock", producto.stock)
                //putExtra("categoria", producto.categoria) -- esto da error falta actualizar y ajustar
                putExtra("url", producto.urlImagen)
            }
            startActivity(intent)
        }

        binding.rvProductos.layoutManager = LinearLayoutManager(context)
        binding.rvProductos.adapter = adapter
    }

    private fun obtenerProductosDeBD(): List<Producto> {
        /*val lista = mutableListOf<Producto>()
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
        //no devolvemos nada solo para probar
        return emptyList()}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
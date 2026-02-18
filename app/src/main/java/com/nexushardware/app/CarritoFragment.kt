package com.nexushardware.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nexushardware.app.databinding.FragmentCarritoBinding
import java.text.NumberFormat
import java.util.Locale

class CarritoFragment : Fragment() {

    private var _binding: FragmentCarritoBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: NexusBDHelper
    private lateinit var adapter: CarritoAdapter
    private var listaItems = mutableListOf<CarritoItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCarritoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = NexusBDHelper(requireContext())

        cargarDatos()

        binding.btnCheckout.setOnClickListener {
            Snackbar.make(binding.root, "Función de Compra Próximamente (OBJ 5)", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun cargarDatos() {
        // Obtenemos los items del usuario 1 (Admin)
        listaItems = dbHelper.obtenerCarrito(1).toMutableList()

        if (listaItems.isEmpty()) {
            binding.rvCarrito.visibility = View.GONE
            binding.tvVacio.visibility = View.VISIBLE
            binding.tvTotalPagar.text = "S/ 0.00"
        } else {
            binding.rvCarrito.visibility = View.VISIBLE
            binding.tvVacio.visibility = View.GONE

            setupRecyclerView()
            calcularTotal()
        }
    }

    private fun setupRecyclerView() {
        adapter = CarritoAdapter(listaItems) { idCarrito, position ->
            eliminarItem(idCarrito, position)
        }
        binding.rvCarrito.layoutManager = LinearLayoutManager(context)
        binding.rvCarrito.adapter = adapter
    }

    private fun calcularTotal() {
        var total = 0.0
        for (item in listaItems) {
            total += (item.precio * item.cantidad)
        }
        val format = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        binding.tvTotalPagar.text = format.format(total)
    }

    private fun eliminarItem(idCarrito: Int, position: Int) {
        //guardamos una copia temporal del producto antes de borrarlo
        val itemBorrado = listaItems[position]

        //lo borramos de la db
        val filas = dbHelper.eliminarItemCarrito(idCarrito)

        if (filas > 0) {
            // lo quitamos de la vista
            adapter.eliminarItem(position)
            calcularTotal()

            if (listaItems.isEmpty()) {
                binding.rvCarrito.visibility = View.GONE
                binding.tvVacio.visibility = View.VISIBLE
            }
            //usamos el snackbar en lugar del toast y muestra la opcion deshacer
            Snackbar.make(binding.root, "${itemBorrado.nombre} eliminado", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {
                    // si el user presiona deshacer lo volvemos a insertar en la db
                    // se usa el user de prueba que es admin
                    dbHelper.agregarAlCarrito(1, itemBorrado.idProducto, itemBorrado.cantidad)
                    //se recarga toda la lista desde la bd para que vuelva a aparecer
                    cargarDatos()
                }
                .setActionTextColor(android.graphics.Color.parseColor("#03DAC5"))
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
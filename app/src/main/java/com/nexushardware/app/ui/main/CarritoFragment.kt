package com.nexushardware.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.nexushardware.app.R
import com.nexushardware.app.data.model.CarritoItem
import com.nexushardware.app.data.local.NexusBDHelper
import com.nexushardware.app.databinding.FragmentCarritoBinding
import com.nexushardware.app.utils.adapters.CarritoAdapter

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

        // Botón Checkout
        binding.btnCheckout.setOnClickListener {
            if (listaItems.isNotEmpty()) {
                realizarCompra()
            } else {
                Snackbar.make(binding.root, "Agrega productos antes de comprar", Snackbar.LENGTH_SHORT).show()
            }
        }

        //Lógica del btn explorar catálogo
        binding.btnExplorar.setOnClickListener {
            //Buscamos la barra de navegación en el MainActivity y cambiamos a la pestaña de productos
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNav.selectedItemId = R.id.nav_productos
        }
    }

    private fun cargarDatos() {
        listaItems = dbHelper.obtenerCarrito(1).toMutableList()

        if (listaItems.isEmpty()) {
            binding.rvCarrito.visibility = View.GONE
            binding.layoutVacio.visibility = View.VISIBLE // Usamos el nuevo layout

            //esto asegura de que el textoy color vuelvan a la normalidsd si se vació manualmente
            binding.tvTituloVacio.text = "Tu carrito está vacío"
            binding.tvTituloVacio.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))

            binding.tvTotalPagar.text = "S/ 0.00"
        } else {
            binding.rvCarrito.visibility = View.VISIBLE
            binding.layoutVacio.visibility = View.GONE

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
        val itemBorrado = listaItems[position]
        val filas = dbHelper.eliminarItemCarrito(idCarrito)

        if (filas > 0) {
            adapter.eliminarItem(position)
            calcularTotal()

            if (listaItems.isEmpty()) {
                binding.rvCarrito.visibility = View.GONE
                binding.layoutVacio.visibility = View.VISIBLE //aqui muestro el nuevo layout
                binding.tvTituloVacio.text = "Tu carrito está vacío"
                binding.tvTituloVacio.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            }

            Snackbar.make(binding.root, "${itemBorrado.nombre} eliminado", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {
                    dbHelper.agregarAlCarrito(1, itemBorrado.idProducto, itemBorrado.cantidad)
                    cargarDatos()
                }
                .setActionTextColor(android.graphics.Color.parseColor("#03DAC5"))
                .show()
        }
    }

    private fun realizarCompra() {
        try {
            // Intentamos procesar la compra
            val productosComprados = dbHelper.procesarCompra(1)

            if (productosComprados > 0) {
                listaItems.clear()
                adapter.notifyDataSetChanged()

                //Actualizamos la interfaz para mostrar el estado vacío pero con éxito
                binding.rvCarrito.visibility = View.GONE
                binding.layoutVacio.visibility = View.VISIBLE

                //reutilizamos el título del estado vacío para felicitar al usuario
                binding.tvTituloVacio.text = "¡Compra exitosa!\nEstamos preparando tu pedido."
                binding.tvTituloVacio.setTextColor(android.graphics.Color.parseColor("#03DAC5"))

                binding.tvTotalPagar.text = "S/ 0.00"

                Snackbar.make(
                    binding.root,
                    "Se procesaron $productosComprados productos.",
                    Snackbar.LENGTH_LONG
                )
                    .setBackgroundTint(android.graphics.Color.parseColor("#03DAC5"))
                    .setTextColor(android.graphics.Color.BLACK)
                    .show()
                }
        }catch (e: NexusBDHelper.StockInsuficienteException) {
                //atrapamos el error
                Snackbar.make(binding.root, "⚠️ ${e.message}", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(android.graphics.Color.parseColor("#CF6679"))
                    .setTextColor(android.graphics.Color.BLACK)
                    .show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
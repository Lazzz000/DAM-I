package com.nexushardware.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexushardware.app.databinding.ItemProductoBinding
import java.text.NumberFormat
import java.util.Locale

class ProductoAdapter(
    private var listaProductos: List<Producto>,
    private val onProductoClick: (Producto) -> Unit // Para manejar clics en el futuro
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(val binding: ItemProductoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        holder.binding.tvNombre.text = producto.nombre
        holder.binding.tvCategoria.text = producto.categoria

        //formateo a soles
        val format = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        holder.binding.tvPrecio.text = format.format(producto.precio)

        // Aquí podríamos cargar la imagen real con Glide usando producto.urlImagen
        // Por ahora usamos el placeholder del XML

        holder.binding.root.setOnClickListener {
            onProductoClick(producto)
        }

        holder.binding.btnAgregar.setOnClickListener {
            // Lógica para añadir al carrito (Próximamente)
        }
    }

    override fun getItemCount(): Int = listaProductos.size

    //Métod para actualizar la lista si filtramos o cambiamos datos
    fun actualizarLista(nuevaLista: List<Producto>) {
        listaProductos = nuevaLista
        notifyDataSetChanged()
    }
}
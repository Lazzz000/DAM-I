package com.nexushardware.app.utils.adapters

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nexushardware.app.data.model.Producto
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

        //cargar imagen con Glide
        Glide.with(holder.itemView.context)
            .load(producto.urlImagen)
            .placeholder(R.drawable.ic_menu_gallery) //imagen temporal mientras carga
            .error(R.drawable.ic_dialog_alert)  //imagen si hay error de reed
            .into(holder.binding.imgProducto)

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
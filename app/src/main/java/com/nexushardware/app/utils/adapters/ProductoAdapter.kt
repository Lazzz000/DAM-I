package com.nexushardware.app.utils.adapters

import com.nexushardware.app.R
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
    private val onProductoClick: (Producto) -> Unit, // Para manejar clics en el futuro
    private val onAgregarCarritoClick: (Producto) -> Unit //NUEVO CALLBACK
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(val binding: ItemProductoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        holder.binding.tvNombre.text = producto.nombre
        // Extraemos de forma segura el texto del objeto. Si llega nulo, ponemos un texto por defecto.
        holder.binding.tvCategoria.text = producto.categoria?.nombreCategoria ?: "Sin Categoría"

        //formateo a soles
        val format = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        holder.binding.tvPrecio.text = format.format(producto.precio)

        //Construimos la URL completa apuntando a tu servidor local con la ruta a la carpeta que se usa en Springboot
        val urlBase = "http://127.0.0.1:8081/img/"
        val urlCompleta = urlBase + producto.urlImagen

        //cargamos la imagen con Glide usando la nueva URL
        Glide.with(holder.itemView.context)
            .load(urlCompleta)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .fallback(R.drawable.ic_image_placeholder)
            .into(holder.binding.imgProducto)

        // Clic en toda la tarjeta
        holder.binding.root.setOnClickListener {
            onProductoClick(producto)
        }

        // 2. Clic EXCLUSIVO en el botón de agregar
        holder.binding.btnAgregar.setOnClickListener {
            onAgregarCarritoClick(producto)
        }
    }

    override fun getItemCount(): Int = listaProductos.size

    //Métod para actualizar la lista si filtramos o cambiamos datos
    fun actualizarLista(nuevaLista: List<Producto>) {
        listaProductos = nuevaLista
        notifyDataSetChanged()
    }
}
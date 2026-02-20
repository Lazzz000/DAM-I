package com.nexushardware.app.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexushardware.app.data.model.CarritoItem
import com.nexushardware.app.databinding.ItemCarritoBinding
import java.text.NumberFormat
import java.util.Locale

import com.bumptech.glide.Glide
class CarritoAdapter (
    private var lista: MutableList<CarritoItem>,
    private val onEliminarClick: (Int, Int) -> Unit // (idCarrito, posicion)
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    inner class CarritoViewHolder(val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val binding = ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.tvNombreCart.text = item.nombre
        holder.binding.tvCantidadCart.text = "Cant: ${item.cantidad}"

        val format = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        holder.binding.tvPrecioCart.text = format.format(item.precio * item.cantidad)

        //Agregar glide para la img de prod en carrito
        Glide.with(holder.itemView.context)
            .load(item.urlImagen.takeIf { it.isNotEmpty() })
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .fallback(android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(holder.binding.imgProductoCart)

        holder.binding.btnEliminar.setOnClickListener {
            onEliminarClick(item.idCarrito, position)
        }
    }

    override fun getItemCount() = lista.size

    fun eliminarItem(position: Int) {
        lista.removeAt(position)
        notifyItemRemoved(position)
    }
}
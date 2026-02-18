package com.nexushardware.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexushardware.app.databinding.ItemCarritoBinding
import java.text.NumberFormat
import java.util.Locale

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
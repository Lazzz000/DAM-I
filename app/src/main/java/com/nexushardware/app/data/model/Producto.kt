package com.nexushardware.app.data.model

import com.google.gson.annotations.SerializedName// importamos el gsonn

//ajustamos la entidad para que coincida con la misma de Springboot(Technova)
data class Producto(
    @SerializedName("idProducto") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("imagen") val urlImagen: String?,
    @SerializedName("categoria") val categoria: Categoria // <-- Aquí unimos las dos tablas
)
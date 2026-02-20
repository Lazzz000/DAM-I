package com.nexushardware.app.data.model

data class CarritoItem(
    val idCarrito: Int,
    val idProducto: Int,
    val nombre: String,
    val precio: Double,
    val cantidad: Int,
    val urlImagen: String
)
package com.nexushardware.app.data.model

import com.google.gson.annotations.SerializedName //importamos gson para la serializacion

data class Categoria(
    @SerializedName("idCategoria") val idCategoria: Int,
    @SerializedName("nombre_Categoria") val nombreCategoria: String
)
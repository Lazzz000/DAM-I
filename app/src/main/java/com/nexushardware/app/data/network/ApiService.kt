package com.nexushardware.app.data.network

import com.nexushardware.app.data.model.Producto
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    // Llamamos al endpoint que acabamos de crear en Spring Boot
    @GET("api/v1/productos")
    suspend fun obtenerProductos(): Response<List<Producto>>

}
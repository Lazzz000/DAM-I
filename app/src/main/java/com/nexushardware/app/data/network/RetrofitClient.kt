package com.nexushardware.app.data.network


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //Como uso depuracion USB(conecto con cable para instalar la app), usamos esta URL local
    private const val BASE_URL = "http://127.0.0.1:8081/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Convierte el JSON a objetos
            .build()
            .create(ApiService::class.java)
    }
}
package com.ieselrincon.allysafe.network

import com.ieselrincon.allysafe.model.*
import retrofit2.Response
import retrofit2.http.*

interface AllySafeApiService {
    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<Usuario>

    @POST("usuarios/registro")
    suspend fun registro(@Body usuario: Usuario): Response<Usuario>

    @GET("categoria/listar")
    suspend fun listarCategorias(): Response<List<Categoria>>

    @GET("alergeno/listar")
    suspend fun listarAlergenos(): Response<List<Alergeno>>

    @GET("alimento/listar")
    suspend fun listarAlimentos(): Response<List<Alimento>>

    @GET("alimento/{id}")
    suspend fun obtenerAlimento(@Path("id") id: Int): Response<Alimento>

    @POST("alimento/guardar")
    suspend fun guardarAlimento(@Body alimento: Alimento): Response<Alimento>

    @PUT("alimento/{id}")
    suspend fun actualizarAlimento(@Path("id") id: Int, @Body alimento: Alimento): Response<Alimento>

    @DELETE("alimento/{id}")
    suspend fun eliminarAlimento(@Path("id") id: Int): Response<Unit>
}
package com.example.monitorco.api

import com.example.monitorco.model.Usuario
import com.example.monitorco.model.Login
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("cadastro")
    suspend fun cadastrarUsuario(@Body usuario: Usuario): Response<Void>

    @POST("login")
    suspend fun login(@Body login: Login): Response<Void>
}

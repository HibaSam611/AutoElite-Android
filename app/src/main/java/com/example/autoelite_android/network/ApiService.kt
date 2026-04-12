package com.example.autoelite_android.network

import com.example.autoelite_android.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<UsuarioResponse>

    @GET("api/auth/me")
    suspend fun getMe(@Header("X-Firebase-UID") uid: String): Response<UsuarioResponse>

    @GET("api/clientes")
    suspend fun getClientes(): Response<List<ClienteResponse>>

    @GET("api/citas/cliente/{clienteId}")
    suspend fun getCitasByCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<CitaResponse>>

    @POST("api/citas")
    suspend fun crearCita(@Body req: CitaRequest): Response<CitaResponse>

    @GET("api/vehiculos/cliente/{clienteId}")
    suspend fun getVehiculosByCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<VehiculoResponse>>

    @POST("api/vehiculos")
    suspend fun crearVehiculo(@Body req: VehiculoRequest): Response<VehiculoResponse>

    @GET("api/reparaciones")
    suspend fun getReparaciones(): Response<List<ReparacionResponse>>

    @GET("api/facturas")
    suspend fun getFacturas(): Response<List<FacturaResponse>>

    @POST("api/valoraciones")
    suspend fun crearValoracion(
        @Header("X-Firebase-UID") uid: String,
        @Body req: ValoracionRequest
    ): Response<Any>

    @PUT("api/citas/{id}/cancelar")
    suspend fun cancelarCita(@Path("id") id: Long): Response<CitaResponse>
}
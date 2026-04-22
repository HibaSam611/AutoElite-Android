package com.example.autoelite_android.network

import com.example.autoelite_android.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<UsuarioResponse>

    @GET("api/auth/me")
    suspend fun getMe(@Header("X-Firebase-UID") uid: String): Response<UsuarioResponse>

    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Header("X-Firebase-UID") uid: String,
        @Body req: UpdateProfileRequest
    ): Response<UsuarioResponse>

    // Clientes
    @GET("api/clientes")
    suspend fun getClientes(): Response<List<ClienteResponse>>

    // citas
    @GET("api/citas/cliente/{clienteId}")
    suspend fun getCitasByCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<CitaResponse>>

    @POST("api/citas")
    suspend fun crearCita(@Body req: CitaRequest): Response<CitaResponse>

    @PUT("api/citas/{id}/cancelar")
    suspend fun cancelarCita(@Path("id") id: Long): Response<CitaResponse>

    // Vehículos
    @GET("api/vehiculos/cliente/{clienteId}")
    suspend fun getVehiculosByCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<VehiculoResponse>>

    @POST("api/vehiculos")
    suspend fun crearVehiculo(@Body req: VehiculoRequest): Response<VehiculoResponse>

    // Reparaciones
    @GET("api/reparaciones")
    suspend fun getReparaciones(): Response<List<ReparacionResponse>>

    @GET("api/reparaciones/cliente/{clienteId}")
    suspend fun getReparacionesByCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<ReparacionResponse>>

    // Facturas
    @GET("api/facturas")
    suspend fun getFacturas(): Response<List<FacturaResponse>>

    @GET("api/facturas/cliente/{clienteId}")
    suspend fun getFacturasByCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<FacturaResponse>>

    // Valoraciones
    @POST("api/valoraciones")
    suspend fun crearValoracion(
        @Header("X-Firebase-UID") uid: String,
        @Body req: ValoracionRequest
    ): Response<Any>

    // Stripe – Pagos online
    @POST("api/pagos/create-payment-intent")
    suspend fun createPaymentIntent(
        @Body req: PaymentIntentRequest
    ): Response<PaymentIntentResponse>

    @POST("api/pagos/confirmar")
    suspend fun confirmarPago(
        @Body req: PaymentConfirmRequest
    ): Response<FacturaResponse>

    // CRM – Canjear recompensas
    @POST("api/crm/canjear")
    suspend fun canjearRecompensa(
        @Body req: CanjearRecompensaRequest
    ): Response<CanjearRecompensaResponse>
}
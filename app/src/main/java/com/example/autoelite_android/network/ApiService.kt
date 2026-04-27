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

    // FCM Token
    @PUT("api/auth/fcm-token")
    suspend fun registrarFcmToken(
        @Header("X-Firebase-UID") uid: String,
        @Body body: Map<String, String>
    ): Response<Map<String, String>>

    // ── Notificaciones in-app ──
    @GET("api/notificaciones")
    suspend fun getNotificaciones(
        @Header("X-Firebase-UID") uid: String
    ): Response<List<Map<String, Any>>>

    @GET("api/notificaciones/no-leidas")
    suspend fun getNotificacionesNoLeidas(
        @Header("X-Firebase-UID") uid: String
    ): Response<Map<String, Any>>

    @PUT("api/notificaciones/leer-todas")
    suspend fun marcarTodasLeidas(
        @Header("X-Firebase-UID") uid: String
    ): Response<Map<String, String>>

    @PUT("api/notificaciones/{id}/leer")
    suspend fun marcarNotificacionLeida(
        @Path("id") id: Long
    ): Response<Map<String, String>>

    // Clientes
    @GET("api/clientes")
    suspend fun getClientes(): Response<List<ClienteResponse>>

    // Citas
    @GET("api/citas/cliente/{clienteId}")
    suspend fun getCitasByCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<CitaResponse>>

    @POST("api/citas")
    suspend fun crearCita(@Body req: CitaRequest): Response<CitaResponse>

    @PUT("api/citas/{id}/cancelar")
    suspend fun cancelarCita(@Path("id") id: Long): Response<CitaResponse>

    @GET("api/citas/horas-disponibles")
    suspend fun getHorasDisponibles(
        @Query("fecha") fecha: String
    ): Response<List<String>>

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

    @GET("api/valoraciones/cliente/{clienteId}")
    suspend fun getValoracionesByCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<ValoracionSimpleResponse>>

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

    // Reparaciones - Aceptar/Rechazar (cliente)
    @PUT("api/reparaciones/{id}/aceptar")
    suspend fun aceptarReparacion(@Path("id") id: Long): Response<ReparacionResponse>

    @PUT("api/reparaciones/{id}/rechazar")
    suspend fun rechazarReparacion(@Path("id") id: Long): Response<ReparacionResponse>
}
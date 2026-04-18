package com.example.autoelite_android.model

data class RegisterRequest(
    val firebaseUid: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null,
    val rol: String = "CLIENTE"
)

data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val rol: String,
    val activo: Boolean
)

data class UpdateProfileRequest(
    val nombre: String,
    val apellidos: String,
    val telefono: String?
)

data class ClienteResponse(
    val id: Long,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val puntosAcumulados: Int,
    val totalGastado: String,
    val fechaRegistro: String
)

data class CitaResponse(
    val id: Long,
    val clienteNombre: String,
    val vehiculo: String,
    val fecha: String,
    val hora: String,
    val tipo: String?,
    val estado: String,
    val descripcion: String?
)

data class CitaRequest(
    val clienteId: Long,
    val vehiculoId: Long,
    val fecha: String,
    val descripcion: String?
)

data class VehiculoResponse(
    val id: Long,
    val clienteId: Long,
    val clienteNombre: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val matricula: String,
    val kilometraje: Int
)

data class VehiculoRequest(
    val clienteId: Long,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val matricula: String,
    val kilometraje: Int = 0
)

data class ReparacionResponse(
    val id: Long,
    val vehiculo: String,
    val matricula: String,
    val clienteNombre: String,
    val mecanico: String,
    val fechaInicio: String,
    val fechaFin: String?,
    val estado: String,
    val costeTotal: String
)

data class FacturaResponse(
    val id: Long,
    val numeroFactura: String,
    val clienteNombre: String,
    val fecha: String,
    val total: String,
    val pagada: Boolean,
    val metodoPago: String?
)

data class ValoracionRequest(
    val reparacionId: Long,
    val puntuacion: Short,
    val comentario: String?
)
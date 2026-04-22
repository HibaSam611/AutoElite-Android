package com.example.autoelite_android.ui.crm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.CanjearRecompensaRequest
import com.example.autoelite_android.model.ClienteResponse
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CrmViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _cliente = MutableStateFlow<ClienteResponse?>(null)
    val cliente: StateFlow<ClienteResponse?> = _cliente

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _canjeando = MutableStateFlow(false)
    val canjeando: StateFlow<Boolean> = _canjeando

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { cargarCliente() }

    fun cargarCliente() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getClientes()
                if (response.isSuccessful) {
                    _cliente.value = response.body()
                        ?.find { it.id == clienteId }
                    _cliente.value?.let { SessionManager.puntos = it.puntosAcumulados }
                }
            } catch (e: Exception) {
                // Usamos datos de SessionManager como fallback
            } finally {
                _loading.value = false
            }
        }
    }

    // Canjear recompensa
    fun canjearRecompensa(nombreRecompensa: String, puntosRequeridos: Int) {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return

        // Validación local rápida
        val puntosActuales = _cliente.value?.puntosAcumulados ?: SessionManager.puntos
        if (puntosActuales < puntosRequeridos) {
            _error.value = "No tienes suficientes puntos"
            return
        }

        viewModelScope.launch {
            _canjeando.value = true
            try {
                val response = api.canjearRecompensa(
                    CanjearRecompensaRequest(
                        clienteId = clienteId,
                        puntosRequeridos = puntosRequeridos,
                        recompensa = nombreRecompensa
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.exito) {
                        // Actualizar puntos localmente
                        SessionManager.puntos = body.puntosRestantes
                        _mensaje.value = body.mensaje
                        // Refrescar datos del cliente desde el servidor
                        cargarCliente()
                    } else {
                        _error.value = body?.mensaje ?: "No se pudo canjear la recompensa"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = when (response.code()) {
                        400 -> "Puntos insuficientes"
                        404 -> "Cliente no encontrado"
                        else -> "Error al canjear (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión con el servidor"
            } finally {
                _canjeando.value = false
            }
        }
    }

    fun resetMensaje() { _mensaje.value = null }
    fun resetError()   { _error.value   = null }
}

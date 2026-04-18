package com.example.autoelite_android.ui.reparaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.ReparacionResponse
import com.example.autoelite_android.model.ValoracionRequest
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReparacionesViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _reparaciones = MutableStateFlow<List<ReparacionResponse>>(emptyList())
    val reparaciones: StateFlow<List<ReparacionResponse>> = _reparaciones

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // IDs de reparaciones ya valoradas en esta sesión
    private val _valoradas = MutableStateFlow<Set<Long>>(emptySet())
    val valoradas: StateFlow<Set<Long>> = _valoradas

    init { cargarReparaciones() }

    fun cargarReparaciones() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return

        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getReparacionesByCliente(clienteId)
                if (response.isSuccessful) {
                    _reparaciones.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar reparaciones"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión con el servidor"
            } finally {
                _loading.value = false
            }
        }
    }

    fun enviarValoracion(reparacionId: Long, puntuacion: Short, comentario: String?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val response = api.crearValoracion(
                    uid = uid,
                    req = ValoracionRequest(
                        reparacionId = reparacionId,
                        puntuacion   = puntuacion,
                        comentario   = comentario
                    )
                )
                if (response.isSuccessful) {
                    _valoradas.value = _valoradas.value + reparacionId
                    _mensaje.value = "Valoración enviada. ¡Gracias!"
                } else {
                    _error.value = "Error al enviar la valoración"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            }
        }
    }

    fun resetError()   { _error.value   = null }
    fun resetMensaje() { _mensaje.value = null }
}
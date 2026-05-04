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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReparacionesViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _reparacionesRaw = MutableStateFlow<List<ReparacionResponse>>(emptyList())

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _valoradas = MutableStateFlow<Set<Long>>(emptySet())
    val valoradas: StateFlow<Set<Long>> = _valoradas

    // ── Filtros ──
    val searchQuery = MutableStateFlow("")
    val estadoFilter = MutableStateFlow<String?>(null)

    val reparaciones: StateFlow<List<ReparacionResponse>> = combine(
        _reparacionesRaw, searchQuery, estadoFilter
    ) { lista, query, estado ->
        lista.filter { rep ->
            val matchEstado = estado == null || rep.estado == estado
            val matchQuery = query.isBlank() ||
                    rep.vehiculo.contains(query, ignoreCase = true) ||
                    rep.matricula.contains(query, ignoreCase = true) ||
                    rep.mecanico.contains(query, ignoreCase = true) ||
                    rep.fechaInicio.contains(query, ignoreCase = true)
            matchEstado && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        cargarReparaciones()
        cargarValoradasExistentes()
    }

    fun cargarReparaciones() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return

        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getReparacionesByCliente(clienteId)
                if (response.isSuccessful) {
                    _reparacionesRaw.value = response.body() ?: emptyList()
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

    fun refresh() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = api.getReparacionesByCliente(clienteId)
                if (response.isSuccessful) {
                    _reparacionesRaw.value = response.body() ?: emptyList()
                }
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }

    private fun cargarValoradasExistentes() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return
        viewModelScope.launch {
            try {
                val response = api.getValoracionesByCliente(clienteId)
                if (response.isSuccessful) {
                    val ids = response.body()
                        ?.map { it.reparacionId }
                        ?.toSet() ?: emptySet()
                    _valoradas.value = _valoradas.value + ids
                }
            } catch (_: Exception) { }
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

    fun aceptarReparacion(reparacionId: Long) {
        viewModelScope.launch {
            try {
                val response = api.aceptarReparacion(reparacionId)
                if (response.isSuccessful) {
                    _mensaje.value = "Reparación aceptada"
                    cargarReparaciones()
                } else {
                    _error.value = "Error al aceptar la reparación"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            }
        }
    }

    fun rechazarReparacion(reparacionId: Long) {
        viewModelScope.launch {
            try {
                val response = api.rechazarReparacion(reparacionId)
                if (response.isSuccessful) {
                    _mensaje.value = "Reparación rechazada"
                    cargarReparaciones()
                } else {
                    _error.value = "Error al rechazar la reparación"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            }
        }
    }

    fun setSearch(query: String) { searchQuery.value = query }
    fun setEstadoFilter(estado: String?) { estadoFilter.value = estado }
    fun resetError()   { _error.value   = null }
    fun resetMensaje() { _mensaje.value = null }
}

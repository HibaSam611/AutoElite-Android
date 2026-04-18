package com.example.autoelite_android.ui.reparaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.ReparacionResponse
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
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

    init { cargarReparaciones() }

    fun cargarReparaciones() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return

        viewModelScope.launch {
            _loading.value = true
            try {
                // Usa el endpoint filtrado por clienteId (seguro)
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

    fun resetError() { _error.value = null }
}
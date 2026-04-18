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
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getReparaciones()
                if (response.isSuccessful) {
                    // Filtramos solo las del cliente actual
                    val clienteNombre = SessionManager.nombre
                    _reparaciones.value = response.body()
                        ?.filter { it.clienteNombre.contains(clienteNombre, ignoreCase = true) }
                        ?: emptyList()
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
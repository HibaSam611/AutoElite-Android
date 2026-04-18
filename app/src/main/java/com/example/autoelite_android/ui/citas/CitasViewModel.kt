package com.example.autoelite_android.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.CitaRequest
import com.example.autoelite_android.model.CitaResponse
import com.example.autoelite_android.model.VehiculoResponse
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CitasViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _citas = MutableStateFlow<List<CitaResponse>>(emptyList())
    val citas: StateFlow<List<CitaResponse>> = _citas

    private val _vehiculos = MutableStateFlow<List<VehiculoResponse>>(emptyList())
    val vehiculos: StateFlow<List<VehiculoResponse>> = _vehiculos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarCitas()
        cargarVehiculos()
    }

    fun cargarCitas() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getCitasByCliente(clienteId)
                if (response.isSuccessful) {
                    _citas.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar citas"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión con el servidor"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun cargarVehiculos() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return
        viewModelScope.launch {
            try {
                val response = api.getVehiculosByCliente(clienteId)
                if (response.isSuccessful) {
                    _vehiculos.value = response.body() ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    fun crearCita(vehiculoId: Long, fecha: String, descripcion: String) {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.crearCita(
                    CitaRequest(
                        clienteId   = clienteId,
                        vehiculoId  = vehiculoId,
                        fecha       = fecha,
                        descripcion = descripcion
                    )
                )
                if (response.isSuccessful) cargarCitas()
                else _error.value = "Error al crear la cita"
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    fun cancelarCita(citaId: Long) {
        viewModelScope.launch {
            try {
                api.cancelarCita(citaId)
                cargarCitas()
            } catch (e: Exception) {
                _error.value = "Error al cancelar"
            }
        }
    }

    fun resetError() { _error.value = null }
}
package com.example.autoelite_android.ui.vehiculos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.VehiculoRequest
import com.example.autoelite_android.model.VehiculoResponse
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VehiculosViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _vehiculos = MutableStateFlow<List<VehiculoResponse>>(emptyList())
    val vehiculos: StateFlow<List<VehiculoResponse>> = _vehiculos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { cargarVehiculos() }

    fun cargarVehiculos() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getVehiculosByCliente(clienteId)
                if (response.isSuccessful) {
                    _vehiculos.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar vehículos"
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
                val response = api.getVehiculosByCliente(clienteId)
                if (response.isSuccessful) {
                    _vehiculos.value = response.body() ?: emptyList()
                }
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }

    fun crearVehiculo(marca: String, modelo: String, anio: Int,
                      matricula: String, kilometraje: Int) {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.crearVehiculo(
                    VehiculoRequest(clienteId = clienteId, marca = marca,
                        modelo = modelo, anio = anio, matricula = matricula,
                        kilometraje = kilometraje)
                )
                if (response.isSuccessful) cargarVehiculos()
                else _error.value = "Error al crear vehículo"
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetError() { _error.value = null }
}
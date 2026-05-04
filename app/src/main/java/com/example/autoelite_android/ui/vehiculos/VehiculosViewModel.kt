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

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

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
                    VehiculoRequest(
                        clienteId = clienteId, marca = marca,
                        modelo = modelo, anio = anio, matricula = matricula,
                        kilometraje = kilometraje
                    )
                )
                if (response.isSuccessful) {
                    _mensaje.value = "Vehículo añadido"
                    cargarVehiculos()
                } else {
                    _error.value = "Error al crear vehículo"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    fun actualizarKilometraje(vehiculo: VehiculoResponse, nuevoKm: Int) {
        viewModelScope.launch {
            try {
                val response = api.actualizarVehiculo(
                    id = vehiculo.id,
                    req = VehiculoRequest(
                        clienteId = vehiculo.clienteId,
                        marca = vehiculo.marca,
                        modelo = vehiculo.modelo,
                        anio = vehiculo.anio,
                        matricula = vehiculo.matricula,
                        kilometraje = nuevoKm
                    )
                )
                if (response.isSuccessful) {
                    _mensaje.value = "Kilometraje actualizado"
                    cargarVehiculos()
                } else {
                    _error.value = "Error al actualizar"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            }
        }
    }

    fun eliminarVehiculo(vehiculoId: Long) {
        viewModelScope.launch {
            try {
                val response = api.eliminarVehiculo(vehiculoId)
                if (response.isSuccessful) {
                    _mensaje.value = "Vehículo eliminado"
                    cargarVehiculos()
                } else {
                    _error.value = when (response.code()) {
                        409 -> "No se puede eliminar: tiene citas o reparaciones asociadas"
                        404 -> "Vehículo no encontrado"
                        else -> "Error al eliminar (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión"
            }
        }
    }

    fun resetError() { _error.value = null }
    fun resetMensaje() { _mensaje.value = null }
}

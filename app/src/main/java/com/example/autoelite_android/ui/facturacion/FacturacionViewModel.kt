package com.example.autoelite_android.ui.facturacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.FacturaResponse
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FacturacionViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _facturas = MutableStateFlow<List<FacturaResponse>>(emptyList())
    val facturas: StateFlow<List<FacturaResponse>> = _facturas

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { cargarFacturas() }

    fun cargarFacturas() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getFacturas()
                if (response.isSuccessful) {
                    val clienteNombre = SessionManager.nombre
                    _facturas.value = response.body()
                        ?.filter { it.clienteNombre.contains(clienteNombre, ignoreCase = true) }
                        ?: emptyList()
                } else {
                    _error.value = "Error al cargar facturas"
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
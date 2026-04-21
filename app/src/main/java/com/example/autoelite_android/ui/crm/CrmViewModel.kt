package com.example.autoelite_android.ui.crm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}
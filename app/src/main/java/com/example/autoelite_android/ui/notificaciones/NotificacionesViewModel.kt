package com.example.autoelite_android.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificacionItem(
    val id: Long,
    val titulo: String,
    val cuerpo: String,
    val pantalla: String,
    val leida: Boolean,
    val fecha: String
)

class NotificacionesViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _notificaciones = MutableStateFlow<List<NotificacionItem>>(emptyList())
    val notificaciones: StateFlow<List<NotificacionItem>> = _notificaciones

    private val _noLeidas = MutableStateFlow(0L)
    val noLeidas: StateFlow<Long> = _noLeidas

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarNotificaciones()
        cargarContadorNoLeidas()
    }

    fun cargarNotificaciones() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getNotificaciones(uid)
                if (response.isSuccessful) {
                    val lista = response.body()?.map { map ->
                        NotificacionItem(
                            id = (map["id"] as Double).toLong(),
                            titulo = map["titulo"] as? String ?: "",
                            cuerpo = map["cuerpo"] as? String ?: "",
                            pantalla = map["pantalla"] as? String ?: "",
                            leida = map["leida"] as? Boolean ?: false,
                            fecha = map["fecha"] as? String ?: ""
                        )
                    } ?: emptyList()
                    _notificaciones.value = lista
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar notificaciones"
            } finally {
                _loading.value = false
            }
        }
    }

    fun cargarContadorNoLeidas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val response = api.getNotificacionesNoLeidas(uid)
                if (response.isSuccessful) {
                    val count = response.body()?.get("count")
                    _noLeidas.value = (count as? Double)?.toLong() ?: 0L
                }
            } catch (_: Exception) { }
        }
    }

    fun marcarTodasLeidas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                api.marcarTodasLeidas(uid)
                _noLeidas.value = 0
                // Actualizar lista local
                _notificaciones.value = _notificaciones.value.map { it.copy(leida = true) }
            } catch (_: Exception) { }
        }
    }

    fun marcarLeida(notificacionId: Long) {
        viewModelScope.launch {
            try {
                api.marcarNotificacionLeida(notificacionId)
                _notificaciones.value = _notificaciones.value.map {
                    if (it.id == notificacionId) it.copy(leida = true) else it
                }
                _noLeidas.value = _notificaciones.value.count { !it.leida }.toLong()
            } catch (_: Exception) { }
        }
    }

    fun resetError() { _error.value = null }
}
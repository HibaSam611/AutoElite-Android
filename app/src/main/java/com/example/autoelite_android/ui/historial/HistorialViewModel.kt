package com.example.autoelite_android.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── Tipos de evento del timeline ──
enum class TipoEvento { CITA, REPARACION, FACTURA_PAGADA, FACTURA_PENDIENTE }

data class TimelineEvent(
    val id: Long,
    val tipo: TipoEvento,
    val titulo: String,
    val subtitulo: String,
    val fecha: String,           // formato mostrable
    val fechaOrdenable: String,  // formato yyyy-MM-dd para ordenar
    val detalle: String? = null,
    val importe: String? = null,
    val estado: String? = null
)

class HistorialViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _eventos = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val eventos: StateFlow<List<TimelineEvent>> = _eventos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { cargarHistorial() }

    fun cargarHistorial() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                // Lanzar las 3 peticiones en paralelo
                val citasDeferred = async { api.getCitasByCliente(clienteId) }
                val reparacionesDeferred = async { api.getReparacionesByCliente(clienteId) }
                val facturasDeferred = async { api.getFacturasByCliente(clienteId) }

                val eventosUnificados = mutableListOf<TimelineEvent>()

                // ── Citas ──
                val citasResponse = citasDeferred.await()
                if (citasResponse.isSuccessful) {
                    citasResponse.body()?.forEach { cita ->
                        eventosUnificados.add(
                            TimelineEvent(
                                id = cita.id,
                                tipo = TipoEvento.CITA,
                                titulo = cita.tipo ?: "Cita de servicio",
                                subtitulo = cita.vehiculo,
                                fecha = "${cita.fecha} · ${cita.hora}",
                                fechaOrdenable = parsearFechaOrdenable(cita.fecha),
                                estado = cita.estado
                            )
                        )
                    }
                }

                // ── Reparaciones ──
                val reparacionesResponse = reparacionesDeferred.await()
                if (reparacionesResponse.isSuccessful) {
                    reparacionesResponse.body()?.forEach { rep ->
                        eventosUnificados.add(
                            TimelineEvent(
                                id = rep.id + 100_000, // evitar colisión de IDs
                                tipo = TipoEvento.REPARACION,
                                titulo = "${rep.vehiculo} · ${rep.matricula}",
                                subtitulo = "Mecánico: ${rep.mecanico}",
                                fecha = rep.fechaInicio + (rep.fechaFin?.let { " → $it" } ?: ""),
                                fechaOrdenable = parsearFechaOrdenable(rep.fechaInicio),
                                estado = rep.estado,
                                importe = "${rep.costeTotal} €"
                            )
                        )
                    }
                }

                // ── Facturas ──
                val facturasResponse = facturasDeferred.await()
                if (facturasResponse.isSuccessful) {
                    facturasResponse.body()?.forEach { factura ->
                        eventosUnificados.add(
                            TimelineEvent(
                                id = factura.id + 200_000,
                                tipo = if (factura.pagada) TipoEvento.FACTURA_PAGADA
                                else TipoEvento.FACTURA_PENDIENTE,
                                titulo = factura.numeroFactura,
                                subtitulo = if (factura.pagada) "Pagada"
                                else "Pendiente de pago",
                                fecha = factura.fecha,
                                fechaOrdenable = parsearFechaOrdenable(factura.fecha),
                                importe = "${factura.total} €",
                                detalle = factura.metodoPago
                            )
                        )
                    }
                }

                // Ordenar por fecha más reciente primero
                _eventos.value = eventosUnificados.sortedByDescending { it.fechaOrdenable }

            } catch (e: Exception) {
                _error.value = "Error al cargar el historial"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Intenta extraer una fecha ordenable (yyyy-MM-dd) desde distintos formatos:
     *  - "dd/MM/yyyy" → "yyyy/MM/dd"
     *  - "yyyy-MM-dd" → tal cual
     *  - otro → devuelve tal cual (peor caso ordena mal pero no crashea)
     */
    private fun parsearFechaOrdenable(fecha: String): String {
        // Formato dd/MM/yyyy
        if (fecha.length >= 10 && fecha[2] == '/') {
            val partes = fecha.take(10).split("/")
            if (partes.size == 3) return "${partes[2]}-${partes[1]}-${partes[0]}"
        }
        // Formato yyyy-MM-dd (o yyyy-MM-ddTHH:mm:ss)
        if (fecha.length >= 10 && fecha[4] == '-') {
            return fecha.take(10)
        }
        return fecha
    }

    fun resetError() { _error.value = null }
}
package com.example.autoelite_android.ui.facturacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.FacturaResponse
import com.example.autoelite_android.model.PaymentConfirmRequest
import com.example.autoelite_android.model.PaymentIntentRequest
import com.example.autoelite_android.model.PaymentIntentResponse
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FacturacionViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _facturasRaw = MutableStateFlow<List<FacturaResponse>>(emptyList())

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // Stripe
    private val _paymentConfig = MutableStateFlow<PaymentIntentResponse?>(null)
    val paymentConfig: StateFlow<PaymentIntentResponse?> = _paymentConfig

    private val _paymentLoading = MutableStateFlow(false)
    val paymentLoading: StateFlow<Boolean> = _paymentLoading

    private var facturaPagandoId: Long? = null
    var lastClientSecret: String? = null
        private set

    // ── Filtros ──
    val searchQuery = MutableStateFlow("")
    val pagoFilter = MutableStateFlow<Boolean?>(null) // null=todas, true=pagadas, false=pendientes

    val facturas: StateFlow<List<FacturaResponse>> = combine(
        _facturasRaw, searchQuery, pagoFilter
    ) { lista, query, pagada ->
        lista.filter { factura ->
            val matchPago = pagada == null || factura.pagada == pagada
            val matchQuery = query.isBlank() ||
                    factura.numeroFactura.contains(query, ignoreCase = true) ||
                    factura.clienteNombre.contains(query, ignoreCase = true) ||
                    factura.fecha.contains(query, ignoreCase = true) ||
                    factura.total.contains(query, ignoreCase = true)
            matchPago && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { cargarFacturas() }

    fun cargarFacturas() {
        val clienteId = SessionManager.clienteId
        if (clienteId == -1L) return

        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getFacturasByCliente(clienteId)
                if (response.isSuccessful) {
                    _facturasRaw.value = response.body() ?: emptyList()
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

    // Stripe – Flujo de pago
    fun iniciarPago(facturaId: Long) {
        viewModelScope.launch {
            _paymentLoading.value = true
            facturaPagandoId = facturaId
            try {
                val response = api.createPaymentIntent(
                    PaymentIntentRequest(facturaId = facturaId)
                )
                if (response.isSuccessful) {
                    _paymentConfig.value = response.body()
                    lastClientSecret = response.body()?.clientSecret
                } else {
                    _error.value = "Error al preparar el pago"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión para procesar el pago"
            } finally {
                _paymentLoading.value = false
            }
        }
    }

    fun onPaymentSuccess(clientSecret: String? = null) {
        val facturaId = facturaPagandoId ?: return
        val paymentIntentId = clientSecret?.substringBefore("_secret_") ?: ""

        viewModelScope.launch {
            try {
                val response = api.confirmarPago(
                    PaymentConfirmRequest(
                        facturaId = facturaId,
                        paymentIntentId = paymentIntentId
                    )
                )
                if (response.isSuccessful) {
                    _mensaje.value = "¡Pago realizado con éxito!"
                } else {
                    _error.value = "Error confirmando pago: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                facturaPagandoId = null
                lastClientSecret = null
                _paymentConfig.value = null
                cargarFacturas()
            }
        }
    }

    fun onPaymentCancelled() {
        facturaPagandoId = null
        lastClientSecret = null
        _paymentConfig.value = null
    }

    fun onPaymentError(errorMsg: String?) {
        facturaPagandoId = null
        lastClientSecret = null
        _paymentConfig.value = null
        _error.value = errorMsg ?: "Error al procesar el pago"
    }

    fun clearPaymentConfig() { _paymentConfig.value = null }

    fun setSearch(query: String) { searchQuery.value = query }
    fun setPagoFilter(pagada: Boolean?) { pagoFilter.value = pagada }
    fun resetError()   { _error.value   = null }
    fun resetMensaje() { _mensaje.value = null }
}

package com.example.autoelite_android.ui.facturacion

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.R
import com.example.autoelite_android.model.FacturaResponse
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.ui.components.FacturaCardSkeleton
import com.example.autoelite_android.ui.components.ShimmerList
import com.example.autoelite_android.util.PdfGenerator
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturacionScreen(
    navController: NavController,
    viewModel: FacturacionViewModel = viewModel()
) {
    val facturas       by viewModel.facturas.collectAsState()
    val loading        by viewModel.loading.collectAsState()
    val isRefreshing   by viewModel.isRefreshing.collectAsState()
    val error          by viewModel.error.collectAsState()
    val mensaje        by viewModel.mensaje.collectAsState()
    val paymentConfig  by viewModel.paymentConfig.collectAsState()
    val paymentLoading by viewModel.paymentLoading.collectAsState()
    val searchQuery    by viewModel.searchQuery.collectAsState()
    val pagoFilter     by viewModel.pagoFilter.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showSearch by remember { mutableStateOf(false) }
    var generandoPdf by remember { mutableStateOf<Long?>(null) }

    val pdfErrorMsg = stringResource(R.string.facturas_pdf_error)

    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> viewModel.onPaymentSuccess(viewModel.lastClientSecret)
            is PaymentSheetResult.Canceled -> viewModel.onPaymentCancelled()
            is PaymentSheetResult.Failed -> viewModel.onPaymentError(result.error.localizedMessage)
        }
    }

    LaunchedEffect(paymentConfig) {
        paymentConfig?.let { config ->
            PaymentConfiguration.init(context, config.publishableKey)
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = config.clientSecret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "AutoElite",
                    customer = PaymentSheet.CustomerConfiguration(
                        id = config.customerId, ephemeralKeySecret = config.ephemeralKey),
                    allowsDelayedPaymentMethods = false))
            viewModel.clearPaymentConfig()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) { error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() } }
    LaunchedEffect(mensaje) { mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.resetMensaje() } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.facturas_title)) }, actions = {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(if (showSearch) Icons.Default.SearchOff else Icons.Default.Search,
                        stringResource(R.string.action_search))
                }
            })
        },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(value = searchQuery, onValueChange = { viewModel.setSearch(it) },
                    placeholder = { Text(stringResource(R.string.facturas_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank())
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                Icon(Icons.Default.Clear, stringResource(R.string.action_clear))
                            }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)) {
                item {
                    FilterChip(
                        selected = pagoFilter == null,
                        onClick = { viewModel.setPagoFilter(null) },
                        label = { Text(stringResource(R.string.action_all)) },
                        leadingIcon = if (pagoFilter == null) {
                            { Icon(Icons.Default.Done, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(selected = pagoFilter == false,
                        onClick = { viewModel.setPagoFilter(if (pagoFilter == false) null else false) },
                        label = { Text(stringResource(R.string.facturas_filter_pending)) },
                        leadingIcon = if (pagoFilter == false) {
                            { Icon(Icons.Default.Done, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(selected = pagoFilter == true,
                        onClick = { viewModel.setPagoFilter(if (pagoFilter == true) null else true) },
                        label = { Text(stringResource(R.string.facturas_filter_paid)) },
                        leadingIcon = if (pagoFilter == true) {
                            { Icon(Icons.Default.Done, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            when {
                loading -> {
                    ShimmerList(count = 4) { FacturaCardSkeleton() }
                }

                facturas.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, null,
                            modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (searchQuery.isNotBlank() || pagoFilter != null)
                                stringResource(R.string.facturas_empty_filtered)
                            else stringResource(R.string.facturas_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                else -> {
                    Text(
                        if (facturas.size == 1) stringResource(R.string.facturas_count_one, facturas.size)
                        else stringResource(R.string.facturas_count_many, facturas.size),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

                    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()) {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)) {
                            items(facturas) { factura ->
                                FacturaCard(factura = factura, paymentLoading = paymentLoading,
                                    generandoPdf = generandoPdf == factura.id,
                                    onPagar = { viewModel.iniciarPago(factura.id) },
                                    onCompartirPdf = {
                                        scope.launch {
                                            generandoPdf = factura.id
                                            try {
                                                val archivo = withContext(Dispatchers.IO) { PdfGenerator.generarFacturaPdf(context, factura) }
                                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
                                                val shareSubject = context.getString(R.string.facturas_share_subject, factura.numeroFactura)
                                                val shareBody = context.getString(R.string.facturas_share_body, factura.numeroFactura, factura.total)
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                                                    putExtra(Intent.EXTRA_TEXT, shareBody)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(intent, shareSubject))
                                            } catch (e: Exception) { snackbarHostState.showSnackbar(pdfErrorMsg) }
                                            finally { generandoPdf = null }
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FacturaCard(factura: FacturaResponse, paymentLoading: Boolean,
                        generandoPdf: Boolean, onPagar: () -> Unit, onCompartirPdf: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, null,
                    tint = if (factura.pagada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(factura.numeroFactura, fontWeight = FontWeight.Bold)
                    Text(factura.fecha, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    factura.metodoPago?.let {
                        Text(stringResource(R.string.facturas_method, it),
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${factura.total} €", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(
                        if (factura.pagada) stringResource(R.string.facturas_paid)
                        else stringResource(R.string.facturas_pending),
                        fontSize = 11.sp,
                        color = if (factura.pagada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCompartirPdf, enabled = !generandoPdf,
                    modifier = if (factura.pagada) Modifier.fillMaxWidth() else Modifier.weight(1f)) {
                    if (generandoPdf) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.facturas_generating), fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.facturas_share_pdf), fontSize = 13.sp)
                    }
                }
                if (!factura.pagada) {
                    Button(onClick = onPagar, enabled = !paymentLoading, modifier = Modifier.weight(1f)) {
                        if (paymentLoading) {
                            CircularProgressIndicator(Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.facturas_paying), fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.CreditCard, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.facturas_pay), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

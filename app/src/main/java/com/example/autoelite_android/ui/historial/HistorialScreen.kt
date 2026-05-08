package com.example.autoelite_android.ui.historial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.R
import com.example.autoelite_android.navigation.AutoEliteBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    navController: NavController,
    viewModel: HistorialViewModel = viewModel()
) {
    val eventos by viewModel.eventos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error   by viewModel.error.collectAsState()

    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.historial_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarHistorial() }) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.action_refresh))
                    }
                }
            )
        },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            loading -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            eventos.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.historial_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.historial_empty_subtitle),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            else -> {
                val eventosAgrupados = eventos.groupBy { evento ->
                    extraerMesAnio(evento.fechaOrdenable, context)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        ResumenCard(eventos)
                        Spacer(Modifier.height(16.dp))
                    }

                    eventosAgrupados.forEach { (mesAnio, eventosDelMes) ->
                        item {
                            Text(mesAnio, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp))
                        }
                        itemsIndexed(eventosDelMes) { index, evento ->
                            TimelineItem(evento = evento, showLine = index != eventosDelMes.lastIndex)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenCard(eventos: List<TimelineEvent>) {
    val totalCitas = eventos.count { it.tipo == TipoEvento.CITA }
    val totalReparaciones = eventos.count { it.tipo == TipoEvento.REPARACION }
    val totalFacturas = eventos.count {
        it.tipo == TipoEvento.FACTURA_PAGADA || it.tipo == TipoEvento.FACTURA_PENDIENTE
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            ResumenItem(Icons.Default.CalendarMonth, "$totalCitas",
                stringResource(R.string.historial_appointments))
            ResumenItem(Icons.Default.Build, "$totalReparaciones",
                stringResource(R.string.historial_repairs))
            ResumenItem(Icons.Default.Receipt, "$totalFacturas",
                stringResource(R.string.historial_invoices))
        }
    }
}

@Composable
private fun ResumenItem(icon: ImageVector, valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(valor, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(etiqueta, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TimelineItem(evento: TimelineEvent, showLine: Boolean) {
    val (icono, color) = eventoIconoYColor(evento.tipo)
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.TopCenter) {
            if (showLine) {
                Canvas(modifier = Modifier.fillMaxHeight().width(2.dp).align(Alignment.TopCenter)) {
                    drawLine(color = lineColor, start = Offset(size.width / 2, 20f),
                        end = Offset(size.width / 2, size.height), strokeWidth = 2f)
                }
            }
            Surface(modifier = Modifier.size(32.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = color.copy(alpha = 0.15f)) {
                Icon(icono, null, tint = color, modifier = Modifier.padding(6.dp))
            }
        }

        Spacer(Modifier.width(8.dp))

        Card(modifier = Modifier.weight(1f).padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(evento.titulo, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, modifier = Modifier.weight(1f))
                    evento.importe?.let {
                        Text(it, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(evento.subtitulo, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(evento.fecha, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    evento.estado?.let { estado ->
                        SuggestionChip(onClick = {},
                            label = { Text(estadoLegible(estado), fontSize = 10.sp) },
                            modifier = Modifier.height(24.dp))
                    }
                }
                evento.detalle?.let {
                    Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun eventoIconoYColor(tipo: TipoEvento): Pair<ImageVector, Color> {
    return when (tipo) {
        TipoEvento.CITA -> Icons.Default.CalendarMonth to MaterialTheme.colorScheme.primary
        TipoEvento.REPARACION -> Icons.Default.Build to MaterialTheme.colorScheme.tertiary
        TipoEvento.FACTURA_PAGADA -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        TipoEvento.FACTURA_PENDIENTE -> Icons.Default.Receipt to MaterialTheme.colorScheme.error
    }
}

@Composable
private fun estadoLegible(estado: String) = when (estado) {
    "PENDIENTE"  -> stringResource(R.string.estado_pendiente)
    "CONFIRMADA" -> stringResource(R.string.estado_confirmada)
    "CANCELADA"  -> stringResource(R.string.estado_cancelada)
    "EN_PROCESO" -> stringResource(R.string.estado_en_proceso)
    "TERMINADA"  -> stringResource(R.string.estado_terminada)
    else -> estado.lowercase().replaceFirstChar { it.uppercase() }
}

private fun extraerMesAnio(fechaOrdenable: String, context: android.content.Context): String {
    if (fechaOrdenable.length < 7) return context.getString(R.string.month_other)
    val partes = fechaOrdenable.split("-")
    if (partes.size < 2) return context.getString(R.string.month_other)
    val mesResId = when (partes[1]) {
        "01" -> R.string.month_01; "02" -> R.string.month_02; "03" -> R.string.month_03
        "04" -> R.string.month_04; "05" -> R.string.month_05; "06" -> R.string.month_06
        "07" -> R.string.month_07; "08" -> R.string.month_08; "09" -> R.string.month_09
        "10" -> R.string.month_10; "11" -> R.string.month_11; "12" -> R.string.month_12
        else -> R.string.month_other
    }
    return "${context.getString(mesResId)} ${partes[0]}"
}

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarHistorial() }) {
                        Icon(Icons.Default.Refresh, "Refrescar")
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
                    Icon(
                        Icons.Default.History, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Aún no hay actividad",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tus citas, reparaciones y pagos aparecerán aquí",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            else -> {
                // Agrupar eventos por mes/año para cabeceras
                val eventosAgrupados = eventos.groupBy { evento ->
                    extraerMesAnio(evento.fechaOrdenable)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Resumen rápido
                    item {
                        ResumenCard(eventos)
                        Spacer(Modifier.height(16.dp))
                    }

                    eventosAgrupados.forEach { (mesAnio, eventosDelMes) ->
                        // Cabecera de mes
                        item {
                            Text(
                                mesAnio,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        itemsIndexed(eventosDelMes) { index, evento ->
                            val esUltimo = index == eventosDelMes.lastIndex
                            TimelineItem(
                                evento = evento,
                                showLine = !esUltimo
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Tarjeta de resumen
// ──────────────────────────────────────────────────────────────
@Composable
private fun ResumenCard(eventos: List<TimelineEvent>) {
    val totalCitas = eventos.count { it.tipo == TipoEvento.CITA }
    val totalReparaciones = eventos.count { it.tipo == TipoEvento.REPARACION }
    val totalFacturas = eventos.count {
        it.tipo == TipoEvento.FACTURA_PAGADA || it.tipo == TipoEvento.FACTURA_PENDIENTE
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ResumenItem(Icons.Default.CalendarMonth, "$totalCitas", "Citas")
            ResumenItem(Icons.Default.Build, "$totalReparaciones", "Reparaciones")
            ResumenItem(Icons.Default.Receipt, "$totalFacturas", "Facturas")
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

// ──────────────────────────────────────────────────────────────
// Elemento del timeline con línea vertical
// ──────────────────────────────────────────────────────────────
@Composable
private fun TimelineItem(evento: TimelineEvent, showLine: Boolean) {
    val (icono, color) = eventoIconoYColor(evento.tipo)
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(modifier = Modifier.fillMaxWidth()) {
        // ── Columna del indicador (punto + línea) ──
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Línea vertical
            if (showLine) {
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .align(Alignment.TopCenter)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 20f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2f
                    )
                }
            }

            // Icono circular
            Surface(
                modifier = Modifier.size(32.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = color.copy(alpha = 0.15f)
            ) {
                Icon(
                    icono, null,
                    tint = color,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // ── Contenido del evento ──
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        evento.titulo,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    evento.importe?.let {
                        Text(
                            it,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    evento.subtitulo,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        evento.fecha,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    evento.estado?.let { estado ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    estadoLegible(estado),
                                    fontSize = 10.sp
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                evento.detalle?.let {
                    Text(
                        it,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

// Helpers
@Composable
private fun eventoIconoYColor(tipo: TipoEvento): Pair<ImageVector, Color> {
    return when (tipo) {
        TipoEvento.CITA -> Icons.Default.CalendarMonth to MaterialTheme.colorScheme.primary
        TipoEvento.REPARACION -> Icons.Default.Build to MaterialTheme.colorScheme.tertiary
        TipoEvento.FACTURA_PAGADA -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        TipoEvento.FACTURA_PENDIENTE -> Icons.Default.Receipt to MaterialTheme.colorScheme.error
    }
}

private fun estadoLegible(estado: String) = when (estado) {
    "PENDIENTE"  -> "Pendiente"
    "CONFIRMADA" -> "Confirmada"
    "CANCELADA"  -> "Cancelada"
    "EN_PROCESO" -> "En proceso"
    "TERMINADA"  -> "Terminada"
    else -> estado.lowercase().replaceFirstChar { it.uppercase() }
}

private fun extraerMesAnio(fechaOrdenable: String): String {
    // fechaOrdenable tiene formato "yyyy-MM-dd"
    if (fechaOrdenable.length < 7) return "Otros"
    val partes = fechaOrdenable.split("-")
    if (partes.size < 2) return "Otros"
    val mes = when (partes[1]) {
        "01" -> "Enero"; "02" -> "Febrero"; "03" -> "Marzo"
        "04" -> "Abril"; "05" -> "Mayo"; "06" -> "Junio"
        "07" -> "Julio"; "08" -> "Agosto"; "09" -> "Septiembre"
        "10" -> "Octubre"; "11" -> "Noviembre"; "12" -> "Diciembre"
        else -> partes[1]
    }
    return "$mes ${partes[0]}"
}
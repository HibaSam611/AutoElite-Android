package com.example.autoelite_android.ui.reparaciones

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.model.ReparacionResponse
import com.example.autoelite_android.navigation.AutoEliteBottomBar

private val estadosPasos = listOf("Recibido", "En proceso", "Terminado", "Confirmado")
private fun estadoIndex(estado: String) = when (estado) {
    "EN_PROCESO" -> 1
    "TERMINADA"  -> 2
    "CONFIRMADA" -> 3
    else         -> 0
}

private val estadosReparacion = listOf("PENDIENTE", "EN_PROCESO", "TERMINADA", "CONFIRMADA")
private fun estadoRepLabel(estado: String) = when (estado) {
    "PENDIENTE"  -> "Pendientes"
    "EN_PROCESO" -> "En proceso"
    "TERMINADA"  -> "Terminadas"
    "CONFIRMADA" -> "Confirmadas"
    else -> estado
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReparacionesScreen(
    navController: NavController,
    viewModel: ReparacionesViewModel = viewModel()
) {
    val reparaciones by viewModel.reparaciones.collectAsState()
    val loading      by viewModel.loading.collectAsState()
    val error        by viewModel.error.collectAsState()
    val mensaje      by viewModel.mensaje.collectAsState()
    val valoradas    by viewModel.valoradas.collectAsState()
    val searchQuery  by viewModel.searchQuery.collectAsState()
    val estadoFilter by viewModel.estadoFilter.collectAsState()

    var reparacionAValorar by remember { mutableStateOf<ReparacionResponse?>(null) }
    var showSearch by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() }
    }
    LaunchedEffect(mensaje) {
        mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.resetMensaje() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reparaciones") },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            if (showSearch) Icons.Default.SearchOff else Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }
                }
            )
        },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Barra de búsqueda ──
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearch(it) },
                    placeholder = { Text("Buscar por vehículo, matrícula, mecánico…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                Icon(Icons.Default.Clear, "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ── Chips de filtro ──
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = estadoFilter == null,
                        onClick = { viewModel.setEstadoFilter(null) },
                        label = { Text("Todas") },
                        leadingIcon = if (estadoFilter == null) {{
                            Icon(Icons.Default.Done, null, Modifier.size(16.dp))
                        }} else null
                    )
                }
                items(estadosReparacion) { estado ->
                    FilterChip(
                        selected = estadoFilter == estado,
                        onClick = {
                            viewModel.setEstadoFilter(
                                if (estadoFilter == estado) null else estado
                            )
                        },
                        label = { Text(estadoRepLabel(estado)) },
                        leadingIcon = if (estadoFilter == estado) {{
                            Icon(Icons.Default.Done, null, Modifier.size(16.dp))
                        }} else null
                    )
                }
            }

            // ── Contenido ──
            when {
                loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                reparaciones.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Build, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (searchQuery.isNotBlank() || estadoFilter != null)
                                "No se encontraron reparaciones"
                            else "Sin reparaciones activas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Text(
                        "${reparaciones.size} reparación${if (reparaciones.size != 1) "es" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(reparaciones) { rep ->
                            ReparacionCard(
                                rep = rep,
                                yaValorada = rep.id in valoradas,
                                onValorar = { reparacionAValorar = rep }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Diálogo de valoración ──
    reparacionAValorar?.let { rep ->
        ValoracionDialog(
            vehiculo = "${rep.vehiculo} · ${rep.matricula}",
            onDismiss = { reparacionAValorar = null },
            onEnviar = { puntuacion, comentario ->
                viewModel.enviarValoracion(rep.id, puntuacion, comentario)
                reparacionAValorar = null
            }
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Card de reparación (sin cambios en la lógica interna)
// ──────────────────────────────────────────────────────────────
@Composable
private fun ReparacionCard(
    rep: ReparacionResponse,
    yaValorada: Boolean,
    onValorar: () -> Unit
) {
    val pasoActual = estadoIndex(rep.estado)
    val puedeValorar = (rep.estado == "TERMINADA" || rep.estado == "CONFIRMADA") && !yaValorada

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${rep.vehiculo} · ${rep.matricula}", fontWeight = FontWeight.Bold)
                    Text("Mecánico: ${rep.mecanico}", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(rep.fechaInicio, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Euro, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text("Coste: ${rep.costeTotal} €", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            Text("Estado del vehículo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                estadosPasos.forEachIndexed { index, paso ->
                    val activo = index <= pasoActual
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (activo) Icons.Default.CheckCircle
                            else Icons.Default.RadioButtonUnchecked,
                            contentDescription = paso,
                            tint = if (activo) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(paso, fontSize = 9.sp,
                            color = if (activo) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline)
                    }
                    if (index < estadosPasos.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.weight(0.5f),
                            color = if (index < pasoActual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            if (puedeValorar) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onValorar, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Star, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Valorar reparación")
                }
            }

            if (yaValorada) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Valoración enviada", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Diálogo de valoración
// ──────────────────────────────────────────────────────────────
@Composable
private fun ValoracionDialog(
    vehiculo: String,
    onDismiss: () -> Unit,
    onEnviar: (Short, String?) -> Unit
) {
    var puntuacion by remember { mutableStateOf(0) }
    var comentario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Valorar reparación") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(vehiculo, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("¿Cómo fue tu experiencia?", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { puntuacion = i }) {
                            Icon(
                                imageVector = if (i <= puntuacion) Icons.Default.Star
                                else Icons.Outlined.StarOutline,
                                contentDescription = "$i estrellas",
                                tint = if (i <= puntuacion) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                if (puntuacion > 0) {
                    Text(
                        when (puntuacion) {
                            1 -> "Muy malo"; 2 -> "Malo"; 3 -> "Normal"
                            4 -> "Bueno"; 5 -> "Excelente"; else -> ""
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEnviar(puntuacion.toShort(), comentario.ifBlank { null }) },
                enabled = puntuacion > 0
            ) { Text("Enviar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
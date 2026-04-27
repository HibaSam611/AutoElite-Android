package com.example.autoelite_android.ui.citas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.model.CitaResponse
import com.example.autoelite_android.model.VehiculoResponse
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

private val estadosCita = listOf("PENDIENTE", "CONFIRMADA", "CANCELADA")
private fun estadoLabel(estado: String) = when (estado) {
    "PENDIENTE"  -> "Pendientes"
    "CONFIRMADA" -> "Confirmadas"
    "CANCELADA"  -> "Canceladas"
    else -> estado
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(
    navController: NavController,
    viewModel: CitasViewModel = viewModel()
) {
    val citas      by viewModel.citas.collectAsState()
    val vehiculos  by viewModel.vehiculos.collectAsState()
    val loading    by viewModel.loading.collectAsState()
    val error      by viewModel.error.collectAsState()
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val estadoFilter  by viewModel.estadoFilter.collectAsState()

    var showDialog  by remember { mutableStateOf(false) }
    var showSearch  by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis citas") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nueva cita") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearch(it) },
                    placeholder = { Text("Buscar por vehículo, tipo, fecha…") },
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

            // Chips de filtro por estado
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
                items(estadosCita) { estado ->
                    FilterChip(
                        selected = estadoFilter == estado,
                        onClick = {
                            viewModel.setEstadoFilter(
                                if (estadoFilter == estado) null else estado
                            )
                        },
                        label = { Text(estadoLabel(estado)) },
                        leadingIcon = if (estadoFilter == estado) {{
                            Icon(Icons.Default.Done, null, Modifier.size(16.dp))
                        }} else null
                    )
                }
            }

            // Contenido
            when {
                loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                citas.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CalendarMonth, null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isNotBlank() || estadoFilter != null)
                                    "No se encontraron citas con esos filtros"
                                else "No tienes citas aún",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchQuery.isBlank() && estadoFilter == null) {
                                TextButton(onClick = { showDialog = true }) {
                                    Text("Pedir una cita")
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Contador de resultados
                    Text(
                        "${citas.size} cita${if (citas.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(citas) { cita ->
                            CitaCard(
                                cita = cita,
                                onCancelar = { viewModel.cancelarCita(cita.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        NuevaCitaDialog(
            vehiculos = vehiculos,
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onConfirmar = { vehiculoId, fecha, desc ->
                viewModel.crearCita(vehiculoId, fecha, desc)
                showDialog = false
            }
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Card de cita
// ──────────────────────────────────────────────────────────────
@Composable
private fun CitaCard(cita: CitaResponse, onCancelar: () -> Unit) {
    val estadoColor = when (cita.estado) {
        "CONFIRMADA" -> MaterialTheme.colorScheme.primary
        "PENDIENTE"  -> MaterialTheme.colorScheme.tertiary
        else         -> MaterialTheme.colorScheme.error
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    cita.fecha.take(2),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    cita.fecha.drop(3).take(3),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(
                modifier = Modifier.width(1.dp).height(48.dp).padding(horizontal = 8.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cita.tipo ?: "Servicio", fontWeight = FontWeight.SemiBold)
                Text(
                    "${cita.hora} h", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(cita.estado, fontSize = 11.sp, color = estadoColor) }
                )
            }
            if (cita.estado != "CANCELADA") {
                IconButton(onClick = onCancelar) {
                    Icon(
                        Icons.Default.Cancel, "Cancelar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Diálogo de nueva cita con selección de fecha + grid de horas
// ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevaCitaDialog(
    vehiculos: List<VehiculoResponse>,
    viewModel: CitasViewModel,
    onDismiss: () -> Unit,
    onConfirmar: (Long, String, String) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }

    var vehiculoSeleccionado by remember { mutableStateOf<VehiculoResponse?>(null) }
    var vehiculoExpanded     by remember { mutableStateOf(false) }

    // Fecha
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState   = rememberDatePickerState()
    var fechaIso by remember { mutableStateOf<String?>(null) }
    val fechaFormateada = remember(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(millis))
        } ?: ""
    }

    // Cuando cambia la fecha, pedir horas disponibles
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val iso = sdf.format(Date(millis))
            fechaIso = iso
            viewModel.cargarHorasDisponibles(iso)
        }
    }

    // Hora seleccionada
    var horaSeleccionada by remember { mutableStateOf<String?>(null) }

    // Limpiar hora al cambiar fecha
    LaunchedEffect(fechaIso) {
        horaSeleccionada = null
    }

    val horasDisponibles by viewModel.horasDisponibles.collectAsState()
    val horasLoading     by viewModel.horasLoading.collectAsState()
    val todasLasHoras    = viewModel.todasLasHoras

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cita") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector de vehículo
                ExposedDropdownMenuBox(
                    expanded = vehiculoExpanded,
                    onExpandedChange = { vehiculoExpanded = it }
                ) {
                    OutlinedTextField(
                        value = vehiculoSeleccionado?.let {
                            "${it.marca} ${it.modelo} · ${it.matricula}"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehículo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehiculoExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = vehiculoExpanded,
                        onDismissRequest = { vehiculoExpanded = false }
                    ) {
                        if (vehiculos.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No tienes vehículos registrados") },
                                onClick = { vehiculoExpanded = false }
                            )
                        } else {
                            vehiculos.forEach { v ->
                                DropdownMenuItem(
                                    text = { Text("${v.marca} ${v.modelo} · ${v.matricula}") },
                                    onClick = {
                                        vehiculoSeleccionado = v
                                        vehiculoExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Selector de fecha ──
                OutlinedTextField(
                    value = fechaFormateada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = remember {
                        androidx.compose.foundation.interaction.MutableInteractionSource()
                    }.also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect { interaction ->
                                if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                    mostrarDatePicker = true
                                }
                            }
                        }
                    }
                )

                // Grid de horas disponibles
                if (fechaIso != null) {
                    Text(
                        "Horarios disponibles:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    if (horasLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    } else if (horasDisponibles.isEmpty()) {
                        // Día completo
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.EventBusy, null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "No hay horas disponibles para este día. Selecciona otra fecha.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        // Mostrar todas las horas: disponibles seleccionables, ocupadas desactivadas
                        // Usamos un FlowRow manual con Rows
                        val chunked = todasLasHoras.chunked(4)
                        chunked.forEach { fila ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                fila.forEach { hora ->
                                    val disponible = hora in horasDisponibles
                                    val seleccionada = hora == horaSeleccionada

                                    if (seleccionada) {
                                        // Hora seleccionada: botón relleno
                                        Button(
                                            onClick = { horaSeleccionada = hora },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(hora, fontSize = 13.sp)
                                        }
                                    } else if (disponible) {
                                        // Hora disponible: botón outlined
                                        OutlinedButton(
                                            onClick = { horaSeleccionada = hora },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(hora, fontSize = 13.sp)
                                        }
                                    } else {
                                        // Hora ocupada: botón desactivado con tachado
                                        OutlinedButton(
                                            onClick = {},
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp),
                                            enabled = false,
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Text(
                                                hora,
                                                fontSize = 13.sp,
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        }
                                    }
                                }
                                // Rellenar con espacios si la fila tiene menos de 4
                                repeat(4 - fila.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }

                        // Leyenda
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LeyendaItem(
                                color = MaterialTheme.colorScheme.surface,
                                borderColor = MaterialTheme.colorScheme.outline,
                                texto = "Disponible"
                            )
                            LeyendaItem(
                                color = MaterialTheme.colorScheme.primary,
                                borderColor = MaterialTheme.colorScheme.primary,
                                texto = "Seleccionada"
                            )
                            LeyendaItem(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                texto = "Ocupada"
                            )
                        }
                    }
                } else {
                    // No se ha seleccionado fecha aún
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Selecciona una fecha para ver los horarios disponibles",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Descripción / tipo de servicio ──
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Tipo de servicio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    "El taller confirmará la disponibilidad",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = vehiculoSeleccionado ?: return@Button
                    val fecha = fechaIso ?: return@Button
                    val hora = horaSeleccionada ?: return@Button
                    val isoFecha = "${fecha}T${hora}:00"
                    onConfirmar(v.id, isoFecha, descripcion)
                },
                enabled = vehiculoSeleccionado != null
                        && fechaIso != null
                        && horaSeleccionada != null
                        && descripcion.isNotBlank()
            ) { Text("Solicitar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    // DatePicker dialog
    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun LeyendaItem(
    color: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
    texto: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = color,
            border = BorderStroke(1.dp, borderColor)
        ) {}
        Spacer(Modifier.width(4.dp))
        Text(texto, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
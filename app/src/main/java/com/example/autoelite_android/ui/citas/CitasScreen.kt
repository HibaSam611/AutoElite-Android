package com.example.autoelite_android.ui.citas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.model.CitaResponse
import com.example.autoelite_android.model.VehiculoResponse
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import java.text.SimpleDateFormat
import java.util.*

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
    var showDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis citas") }) },
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
        when {
            loading -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            citas.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
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
                            "No tienes citas aún",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showDialog = true }) {
                            Text("Pedir una cita")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
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

    if (showDialog) {
        NuevaCitaDialog(
            vehiculos = vehiculos,
            onDismiss = { showDialog = false },
            onConfirmar = { vehiculoId, fecha, desc ->
                viewModel.crearCita(vehiculoId, fecha, desc)
                showDialog = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Card de cita
// ─────────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────
// Diálogo de nueva cita con DatePicker, TimePicker y Dropdown
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevaCitaDialog(
    vehiculos: List<VehiculoResponse>,
    onDismiss: () -> Unit,
    onConfirmar: (Long, String, String) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }

    // ── Vehículo seleccionado ──
    var vehiculoSeleccionado by remember { mutableStateOf<VehiculoResponse?>(null) }
    var vehiculoExpanded     by remember { mutableStateOf(false) }

    // ── Fecha ──
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState   = rememberDatePickerState()
    val fechaFormateada = remember(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(millis))
        } ?: ""
    }

    // ── Hora ──
    var mostrarTimePicker by remember { mutableStateOf(false) }
    val timePickerState   = rememberTimePickerState(
        initialHour = 9,
        initialMinute = 0,
        is24Hour = true
    )
    val horaFormateada = remember(
        timePickerState.hour,
        timePickerState.minute,
        mostrarTimePicker      // recalcular al cerrar el picker
    ) {
        String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
    }

    // Diálogo principal
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // ── Dropdown de vehículos ──
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
                                    text = {
                                        Text("${v.marca} ${v.modelo} · ${v.matricula}")
                                    },
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
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect { interaction ->
                                    if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                        mostrarDatePicker = true
                                    }
                                }
                            }
                        }
                )

                // ── Selector de hora ──
                OutlinedTextField(
                    value = horaFormateada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hora") },
                    leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect { interaction ->
                                    if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                        mostrarTimePicker = true
                                    }
                                }
                            }
                        }
                )

                // ── Descripción ──
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
                    // Formato ISO para el backend: "2026-05-10T09:00:00"
                    val isoFecha = datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        "${sdf.format(Date(millis))}T${horaFormateada}:00"
                    } ?: return@Button
                    onConfirmar(v.id, isoFecha, descripcion)
                },
                enabled = vehiculoSeleccionado != null
                        && datePickerState.selectedDateMillis != null
                        && descripcion.isNotBlank()
            ) { Text("Solicitar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    // ── DatePicker modal ──
    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── TimePicker modal ──
    if (mostrarTimePicker) {
        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            title = { Text("Selecciona la hora") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarTimePicker = false }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

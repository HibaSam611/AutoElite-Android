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
import com.example.autoelite_android.navigation.AutoEliteBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(
    navController: NavController,
    viewModel: CitasViewModel = viewModel()
) {
    val citas   by viewModel.citas.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error   by viewModel.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Mostrar error en snackbar
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
                Box(Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            citas.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarMonth, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(12.dp))
                        Text("No tienes citas aún",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { showDialog = true }) {
                            Text("Pedir una cita")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .padding(paddingValues).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(citas) { cita ->
                        CitaCard(cita = cita,
                            onCancelar = { viewModel.cancelarCita(cita.id) })
                    }
                }
            }
        }
    }

    if (showDialog) {
        NuevaCitaDialog(
            onDismiss = { showDialog = false },
            onConfirmar = { vehiculoId, fecha, desc ->
                viewModel.crearCita(vehiculoId, fecha, desc)
                showDialog = false
            }
        )
    }
}

@Composable
private fun CitaCard(cita: CitaResponse, onCancelar: () -> Unit) {
    val estadoColor = when (cita.estado) {
        "CONFIRMADA" -> MaterialTheme.colorScheme.primary
        "PENDIENTE"  -> MaterialTheme.colorScheme.tertiary
        else         -> MaterialTheme.colorScheme.error
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)) {
                Text(cita.fecha.take(2), fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                Text(cita.fecha.drop(3).take(3), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Divider(modifier = Modifier.width(1.dp).height(48.dp).padding(horizontal = 8.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cita.tipo ?: "Servicio", fontWeight = FontWeight.SemiBold)
                Text("${cita.hora} h", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                SuggestionChip(onClick = {},
                    label = { Text(cita.estado, fontSize = 11.sp, color = estadoColor) })
            }
            if (cita.estado != "CANCELADA") {
                IconButton(onClick = onCancelar) {
                    Icon(Icons.Default.Cancel, "Cancelar",
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun NuevaCitaDialog(
    onDismiss: () -> Unit,
    onConfirmar: (Long, String, String) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var fecha       by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Tipo de servicio") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (ej: 2026-05-10T10:00:00)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("El taller confirmará la disponibilidad",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(1L, fecha, descripcion) },
                enabled = descripcion.isNotBlank() && fecha.isNotBlank()
            ) { Text("Solicitar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
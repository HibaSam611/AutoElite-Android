package com.example.autoelite_android.ui.citas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import kotlin.text.drop
import kotlin.text.isNotBlank
import kotlin.text.take

data class CitaUi(
    val id: Int,
    val fecha: String,
    val hora: String,
    val tipo: String,
    val estado: String   // PENDIENTE | CONFIRMADA | CANCELADA
)

private val citasEjemplo = listOf(
    CitaUi(1, "12 Abr 2026", "10:00", "Revisión general", "CONFIRMADA"),
    CitaUi(2, "20 Abr 2026", "12:00", "Cambio de aceite", "PENDIENTE"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis citas") })
        },
        bottomBar = { AutoEliteBottomBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva cita") }
            )
        }
    ) { paddingValues ->
        if (citasEjemplo.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CalendarMonth, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No tienes citas aún", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showDialog = true }) { Text("Pedir una cita") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(citasEjemplo) { cita ->
                    CitaCard(cita = cita, onCancelar = { /* TODO: llamar API */ })
                }
            }
        }
    }

    if (showDialog) {
        NuevaCitaDialog(
            onDismiss = { showDialog = false },
            onConfirmar = { /* llamar API */ showDialog = false }
        )
    }
}

@Composable
private fun CitaCard(cita: CitaUi, onCancelar: () -> Unit) {
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
            // Fecha
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    cita.fecha.take(2), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    cita.fecha.drop(3).take(3), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Divider(modifier = Modifier.width(1.dp).height(48.dp).padding(horizontal = 8.dp))
            Spacer(Modifier.width(8.dp))
            // Detalles
            Column(modifier = Modifier.weight(1f)) {
                Text(cita.tipo, fontWeight = FontWeight.SemiBold)
                Text(
                    "${cita.hora} h",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(cita.estado, fontSize = 11.sp, color = estadoColor) }
                )
            }
            // Cancelar
            if (cita.estado != "CANCELADA") {
                IconButton(onClick = onCancelar) {
                    Icon(
                        Icons.Default.Cancel, contentDescription = "Cancelar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun NuevaCitaDialog(onDismiss: () -> Unit, onConfirmar: () -> Unit) {
    var tipo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecciona el tipo de servicio y fecha. Tu taller confirmará la disponibilidad.")
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo de servicio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // DatePicker, VehiculoPicker, ...
                Text(
                    " Selección de fecha: próximamente", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmar, enabled = tipo.isNotBlank()) { Text("Solicitar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

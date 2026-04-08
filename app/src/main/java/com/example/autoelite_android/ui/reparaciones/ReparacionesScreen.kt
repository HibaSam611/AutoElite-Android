package com.example.autoelite_android.ui.reparaciones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar

data class ReparacionUi(
    val id: Int,
    val vehiculo: String,
    val descripcion: String,
    val estado: String,   // EN_PROCESO | TERMINADA | CONFIRMADA
    val fechaInicio: String,
    val mecanico: String
)

private val reparacionesEjemplo = listOf(
    ReparacionUi(1, "Toyota Corolla · 1234 ABC", "Cambio pastillas de freno + revisión general",
        "EN_PROCESO", "10 Abr 2026", "Carlos Méndez"),
    ReparacionUi(2, "Seat León · 5678 XYZ", "Cambio de aceite y filtros",
        "TERMINADA", "5 Mar 2026", "Luis García"),
)

// Pasos del estado de reparación
private val estadosPasos = listOf("Recibido", "En proceso", "Terminado", "Confirmado")
private fun estadoIndex(estado: String) = when (estado) {
    "EN_PROCESO"  -> 1
    "TERMINADA"   -> 2
    "CONFIRMADA"  -> 3
    else          -> 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReparacionesScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis reparaciones") }) },
        bottomBar = { AutoEliteBottomBar(navController) }
    ) { paddingValues ->
        if (reparacionesEjemplo.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Build, contentDescription = null,
                        modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(12.dp))
                    Text("Sin reparaciones activas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(reparacionesEjemplo) { rep ->
                    ReparacionCard(rep)
                }
            }
        }
    }
}

@Composable
private fun ReparacionCard(rep: ReparacionUi) {
    val pasoActual = estadoIndex(rep.estado)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rep.vehiculo, fontWeight = FontWeight.Bold)
                    Text("Mecánico: ${rep.mecanico}", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(rep.fechaInicio, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.height(8.dp))
            Text(rep.descripcion, fontSize = 13.sp)

            Spacer(Modifier.height(16.dp))

            // ── Stepper de estado ─────────────────────────────────────────────
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)) {
                        Icon(
                            if (activo) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
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
                        Divider(modifier = Modifier.weight(0.5f),
                            color = if (index < pasoActual) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

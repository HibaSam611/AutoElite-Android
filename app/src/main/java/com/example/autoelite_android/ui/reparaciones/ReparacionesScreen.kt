package com.example.autoelite_android.ui.reparaciones

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
import com.example.autoelite_android.model.ReparacionResponse
import com.example.autoelite_android.navigation.AutoEliteBottomBar

private val estadosPasos = listOf("Recibido", "En proceso", "Terminado", "Confirmado")
private fun estadoIndex(estado: String) = when (estado) {
    "EN_PROCESO"  -> 1
    "TERMINADA"   -> 2
    "CONFIRMADA"  -> 3
    else          -> 0
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

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis reparaciones") }) },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            loading -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            reparaciones.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Build, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(12.dp))
                    Text("Sin reparaciones activas",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(reparaciones) { ReparacionCard(it) }
            }
        }
    }
}

@Composable
private fun ReparacionCard(rep: ReparacionResponse) {
    val pasoActual = estadoIndex(rep.estado)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, null,
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${rep.vehiculo} · ${rep.matricula}",
                        fontWeight = FontWeight.Bold)
                    Text("Mecánico: ${rep.mecanico}", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(rep.fechaInicio, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.height(8.dp))

            // Coste
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Euro, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text("Coste: ${rep.costeTotal} €", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            // Stepper de estado
            Text("Estado del vehículo", fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
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
                        Divider(
                            modifier = Modifier.weight(0.5f),
                            color = if (index < pasoActual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}
package com.example.autoelite_android.ui.vehiculos

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
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar

data class VehiculoUi(
    val id: Int,
    val marca: String,
    val modelo: String,
    val anio: String,
    val matricula: String,
    val kilometraje: String
)

private val vehiculosEjemplo = listOf(
    VehiculoUi(1, "Toyota", "Corolla", "2019", "1234 ABC", "82.000 km"),
    VehiculoUi(2, "Seat",   "León",    "2021", "5678 XYZ", "34.500 km"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis vehículos") }) },
        bottomBar = { AutoEliteBottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir vehículo")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(vehiculosEjemplo) { vehiculo ->
                VehiculoCard(vehiculo)
            }
        }
    }

    if (showDialog) {
        AñadirVehiculoDialog(
            onDismiss = { showDialog = false },
            onGuardar = { /* TODO: llamar API */ showDialog = false }
        )
    }
}

@Composable
private fun VehiculoCard(v: VehiculoUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${v.marca} ${v.modelo}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${v.anio}  ·  ${v.matricula}", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(v.kilometraje, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = { /* TODO: eliminar */ }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AñadirVehiculoDialog(onDismiss: () -> Unit, onGuardar: () -> Unit) {
    var marca       by remember { mutableStateOf("") }
    var modelo      by remember { mutableStateOf("") }
    var anio        by remember { mutableStateOf("") }
    var matricula   by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir vehículo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = marca,      onValueChange = { marca = it },
                    label = { Text("Marca") },         modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = modelo,     onValueChange = { modelo = it },
                    label = { Text("Modelo") },        modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = anio,       onValueChange = { anio = it },
                    label = { Text("Año") },           modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = matricula,  onValueChange = { matricula = it },
                    label = { Text("Matrícula") },     modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = kilometraje,onValueChange = { kilometraje = it },
                    label = { Text("Kilometraje") },   modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = onGuardar,
                enabled = marca.isNotBlank() && modelo.isNotBlank() && matricula.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

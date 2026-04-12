package com.example.autoelite_android.ui.vehiculos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.model.VehiculoResponse
import com.example.autoelite_android.navigation.AutoEliteBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosScreen(
    navController: NavController,
    viewModel: VehiculosViewModel = viewModel()
) {
    val vehiculos by viewModel.vehiculos.collectAsState()
    val loading   by viewModel.loading.collectAsState()
    val error     by viewModel.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis vehículos") }) },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Añadir")
            }
        }
    ) { paddingValues ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            vehiculos.isEmpty() -> Box(Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DirectionsCar, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(12.dp))
                    Text("No tienes vehículos registrados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showDialog = true }) { Text("Añadir vehículo") }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(vehiculos) { VehiculoCard(it) }
            }
        }
    }

    if (showDialog) {
        AñadirVehiculoDialog(
            onDismiss = { showDialog = false },
            onGuardar = { marca, modelo, anio, matricula, km ->
                viewModel.crearVehiculo(marca, modelo, anio, matricula, km)
                showDialog = false
            }
        )
    }
}

@Composable
private fun VehiculoCard(v: VehiculoResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${v.marca} ${v.modelo}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${v.anio}  ·  ${v.matricula}", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${v.kilometraje} km", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun AñadirVehiculoDialog(
    onDismiss: () -> Unit,
    onGuardar: (String, String, Int, String, Int) -> Unit
) {
    var marca       by remember { mutableStateOf("") }
    var modelo      by remember { mutableStateOf("") }
    var anio        by remember { mutableStateOf("") }
    var matricula   by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir vehículo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = marca, onValueChange = { marca = it },
                    label = { Text("Marca") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = modelo, onValueChange = { modelo = it },
                    label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = anio, onValueChange = { anio = it },
                    label = { Text("Año") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = matricula, onValueChange = { matricula = it },
                    label = { Text("Matrícula") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = kilometraje, onValueChange = { kilometraje = it },
                    label = { Text("Kilometraje") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(marca, modelo,
                        anio.toIntOrNull() ?: 2020,
                        matricula,
                        kilometraje.toIntOrNull() ?: 0)
                },
                enabled = marca.isNotBlank() && modelo.isNotBlank() && matricula.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
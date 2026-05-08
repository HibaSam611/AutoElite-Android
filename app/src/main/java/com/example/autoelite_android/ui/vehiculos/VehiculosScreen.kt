package com.example.autoelite_android.ui.vehiculos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.R
import com.example.autoelite_android.model.VehiculoResponse
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.ui.components.ShimmerList
import com.example.autoelite_android.ui.components.VehiculoCardSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosScreen(
    navController: NavController,
    viewModel: VehiculosViewModel = viewModel()
) {
    val vehiculos    by viewModel.vehiculos.collectAsState()
    val loading      by viewModel.loading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error        by viewModel.error.collectAsState()
    val mensaje      by viewModel.mensaje.collectAsState()

    var showDialog         by remember { mutableStateOf(false) }
    var vehiculoAEditar    by remember { mutableStateOf<VehiculoResponse?>(null) }
    var vehiculoAEliminar  by remember { mutableStateOf<VehiculoResponse?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() }
    }
    LaunchedEffect(mensaje) {
        mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.resetMensaje() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.vehiculos_title)) }) },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, stringResource(R.string.vehiculos_add))
            }
        }
    ) { paddingValues ->
        when {
            loading -> {
                ShimmerList(count = 4, modifier = Modifier.padding(paddingValues)) {
                    VehiculoCardSkeleton()
                }
            }

            vehiculos.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DirectionsCar, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.vehiculos_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showDialog = true }) {
                        Text(stringResource(R.string.vehiculos_add_vehicle))
                    }
                }
            }

            else -> PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(vehiculos) { vehiculo ->
                        VehiculoCard(
                            v = vehiculo,
                            onEditarKm = { vehiculoAEditar = vehiculo },
                            onEliminar = { vehiculoAEliminar = vehiculo }
                        )
                    }
                }
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

    vehiculoAEditar?.let { vehiculo ->
        EditarKilometrajeDialog(
            vehiculo = vehiculo,
            onDismiss = { vehiculoAEditar = null },
            onGuardar = { nuevoKm ->
                viewModel.actualizarKilometraje(vehiculo, nuevoKm)
                vehiculoAEditar = null
            }
        )
    }

    vehiculoAEliminar?.let { vehiculo ->
        AlertDialog(
            onDismissRequest = { vehiculoAEliminar = null },
            icon = {
                Icon(Icons.Default.DeleteForever, null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp))
            },
            title = { Text(stringResource(R.string.vehiculos_delete_title)) },
            text = {
                Text(stringResource(R.string.vehiculos_delete_confirm,
                    vehiculo.marca, vehiculo.modelo, vehiculo.matricula) +
                        stringResource(R.string.vehiculos_delete_warning))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarVehiculo(vehiculo.id)
                        vehiculoAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { vehiculoAEliminar = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun VehiculoCard(v: VehiculoResponse, onEditarKm: () -> Unit, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DirectionsCar, null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${v.marca} ${v.modelo}",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${v.anio}  ·  ${v.matricula}",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(4.dp))
                        Text("${v.kilometraje} ${stringResource(R.string.vehiculos_km_suffix)}",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEditarKm, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.vehiculos_update_km), fontSize = 12.sp)
                }
                OutlinedButton(onClick = onEliminar, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_delete), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EditarKilometrajeDialog(vehiculo: VehiculoResponse, onDismiss: () -> Unit, onGuardar: (Int) -> Unit) {
    var kilometraje by remember { mutableStateOf(vehiculo.kilometraje.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    val errorInvalid = stringResource(R.string.vehiculos_invalid_km)
    val errorNegative = stringResource(R.string.vehiculos_negative_km)
    val errorLower = stringResource(R.string.vehiculos_lower_km, vehiculo.kilometraje)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Speed, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp))
        },
        title = { Text(stringResource(R.string.vehiculos_update_km_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${vehiculo.marca} ${vehiculo.modelo} · ${vehiculo.matricula}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.vehiculos_current_km, vehiculo.kilometraje),
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                OutlinedTextField(
                    value = kilometraje,
                    onValueChange = { kilometraje = it; error = null },
                    label = { Text(stringResource(R.string.vehiculos_new_km)) },
                    suffix = { Text(stringResource(R.string.vehiculos_km_suffix)) },
                    leadingIcon = { Icon(Icons.Default.Speed, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val km = kilometraje.toIntOrNull()
                when {
                    km == null -> error = errorInvalid
                    km < 0 -> error = errorNegative
                    km < vehiculo.kilometraje -> error = errorLower
                    else -> onGuardar(km)
                }
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun AñadirVehiculoDialog(onDismiss: () -> Unit, onGuardar: (String, String, Int, String, Int) -> Unit) {
    var marca       by remember { mutableStateOf("") }
    var modelo      by remember { mutableStateOf("") }
    var anio        by remember { mutableStateOf("") }
    var matricula   by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.vehiculos_add_vehicle)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = marca, onValueChange = { marca = it },
                    label = { Text(stringResource(R.string.vehiculos_brand)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = modelo, onValueChange = { modelo = it },
                    label = { Text(stringResource(R.string.vehiculos_model)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = anio, onValueChange = { anio = it },
                    label = { Text(stringResource(R.string.vehiculos_year)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = matricula, onValueChange = { matricula = it },
                    label = { Text(stringResource(R.string.vehiculos_plate)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = kilometraje, onValueChange = { kilometraje = it },
                    label = { Text(stringResource(R.string.vehiculos_mileage)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = {
                onGuardar(marca, modelo, anio.toIntOrNull() ?: 2020, matricula, kilometraje.toIntOrNull() ?: 0)
            }, enabled = marca.isNotBlank() && modelo.isNotBlank() && matricula.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

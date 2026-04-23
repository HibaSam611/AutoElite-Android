package com.example.autoelite_android.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.navigation.Screen
import com.example.autoelite_android.ui.citas.CitasViewModel
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    citasViewModel: CitasViewModel = viewModel()
) {
    val user   = FirebaseAuth.getInstance().currentUser
    val nombre = user?.displayName?.split(" ")?.firstOrNull()
        ?: SessionManager.nombre.ifBlank { "Cliente" }

    val citas by citasViewModel.citas.collectAsState()
    val proximaCita = citas.firstOrNull {
        it.estado == "PENDIENTE" || it.estado == "CONFIRMADA"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AutoElite") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Perfil.route)
                    }) {
                        Icon(Icons.Default.AccountCircle, "Perfil")
                    }
                }
            )
        },
        bottomBar = { AutoEliteBottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Hola, $nombre 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("¿Qué necesitas hoy?", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(24.dp))

            Text("Acceso rápido", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickCard(Modifier.weight(1f), Icons.Default.CalendarMonth,
                    "Pedir cita", "Agenda tu próxima visita") {
                    navController.navigate(Screen.Citas.route)
                }
                QuickCard(Modifier.weight(1f), Icons.Default.Build,
                    "Reparaciones", "Estado en tiempo real") {
                    navController.navigate(Screen.Reparaciones.route)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickCard(Modifier.weight(1f), Icons.Default.DirectionsCar,
                    "Mis vehículos", "Gestiona tu flota") {
                    navController.navigate(Screen.Vehiculos.route)
                }
                QuickCard(Modifier.weight(1f), Icons.Default.Star,
                    "Puntos CRM", "Canjea tus recompensas") {
                    navController.navigate(Screen.Crm.route)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickCard(Modifier.weight(1f), Icons.Default.Receipt,
                    "Facturas", "Paga tus facturas online") {
                    navController.navigate(Screen.Facturacion.route)
                }
                QuickCard(Modifier.weight(1f), Icons.Default.History,
                    "Historial", "Tu actividad completa") {
                    navController.navigate(Screen.Historial.route)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Próxima cita", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Event, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(12.dp))
                    if (proximaCita != null) {
                        Column {
                            Text("${proximaCita.fecha} a las ${proximaCita.hora}",
                                fontWeight = FontWeight.Medium)
                            Text(proximaCita.tipo ?: "Servicio general",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Estado: ${proximaCita.estado}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Column {
                            Text("No tienes citas programadas",
                                fontWeight = FontWeight.Medium)
                            Text("Pulsa 'Pedir cita' para agendar una visita",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

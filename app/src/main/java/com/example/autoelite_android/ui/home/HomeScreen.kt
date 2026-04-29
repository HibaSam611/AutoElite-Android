package com.example.autoelite_android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.navigation.Screen
import com.example.autoelite_android.ui.citas.CitasViewModel
import com.example.autoelite_android.ui.notificaciones.NotificacionesViewModel
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    citasViewModel: CitasViewModel = viewModel(),
    notificacionesViewModel: NotificacionesViewModel = viewModel()
) {
    val user   = FirebaseAuth.getInstance().currentUser
    val nombre = user?.displayName?.split(" ")?.firstOrNull()
        ?: SessionManager.nombre.ifBlank { "Cliente" }

    val citas by citasViewModel.citas.collectAsState()
    val proximaCita = citas.firstOrNull {
        it.estado == "PENDIENTE" || it.estado == "CONFIRMADA"
    }

    val noLeidas by notificacionesViewModel.noLeidas.collectAsState()

    LaunchedEffect(Unit) {
        notificacionesViewModel.cargarContadorNoLeidas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "AutoElite",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Notificaciones.route)
                    }) {
                        BadgedBox(
                            badge = {
                                if (noLeidas > 0) {
                                    Badge { Text(if (noLeidas > 99) "99+" else noLeidas.toString(), fontSize = 10.sp) }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, "Notificaciones")
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Perfil.route) }) {
                        Icon(Icons.Outlined.AccountCircle, "Perfil")
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
        ) {
            // ── Header con saludo ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Text(
                        "Hola, $nombre",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Bienvenido a tu taller de confianza",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    // Próxima cita inline
                    if (proximaCita != null) {
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Event, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "Próxima cita",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        "${proximaCita.fecha} · ${proximaCita.hora}",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        proximaCita.tipo ?: "Servicio",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        proximaCita.estado,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Acceso rápido ──
            Text(
                "Acceso rápido",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            // Fila 1
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    Modifier.weight(1f),
                    Icons.Outlined.CalendarMonth,
                    "Pedir cita",
                    "Agenda tu visita",
                    MaterialTheme.colorScheme.primary
                ) { navController.navigate(Screen.Citas.route) }

                QuickCard(
                    Modifier.weight(1f),
                    Icons.Outlined.Build,
                    "Reparaciones",
                    "Seguimiento en vivo",
                    MaterialTheme.colorScheme.tertiary
                ) { navController.navigate(Screen.Reparaciones.route) }
            }

            Spacer(Modifier.height(12.dp))

            // Fila 2
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    Modifier.weight(1f),
                    Icons.Outlined.DirectionsCar,
                    "Vehículos",
                    "Gestiona tu flota",
                    MaterialTheme.colorScheme.secondary
                ) { navController.navigate(Screen.Vehiculos.route) }

                QuickCard(
                    Modifier.weight(1f),
                    Icons.Outlined.Star,
                    "Puntos CRM",
                    "Canjea recompensas",
                    MaterialTheme.colorScheme.tertiary
                ) { navController.navigate(Screen.Crm.route) }
            }

            Spacer(Modifier.height(12.dp))

            // Fila 3
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    Modifier.weight(1f),
                    Icons.Outlined.Receipt,
                    "Facturas",
                    "Paga online",
                    MaterialTheme.colorScheme.error
                ) { navController.navigate(Screen.Facturacion.route) }

                QuickCard(
                    Modifier.weight(1f),
                    Icons.Outlined.History,
                    "Historial",
                    "Tu actividad",
                    MaterialTheme.colorScheme.outline
                ) { navController.navigate(Screen.Historial.route) }
            }

            // Si no hay próxima cita, mostrar CTA
            if (proximaCita == null) {
                Spacer(Modifier.height(20.dp))
                Card(
                    onClick = { navController.navigate(Screen.Citas.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.EventAvailable, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Sin citas programadas",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Pide tu próxima cita ahora",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward, null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    icon, null,
                    tint = accentColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
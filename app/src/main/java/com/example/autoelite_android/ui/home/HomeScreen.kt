package com.example.autoelite.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val nombre = user?.displayName?.split(" ")?.firstOrNull() ?: "Cliente"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AutoElite") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Perfil.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
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
            //Saludo
            Text(
                text = "Hola, $nombre 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "¿Qué necesitas hoy?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Tarjetas de acceso rápido
            Text("Acceso rápido", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CalendarMonth,
                    title = "Pedir cita",
                    subtitle = "Agenda tu próxima visita",
                    onClick = { navController.navigate(Screen.Citas.route) }
                )
                QuickCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Build,
                    title = "Mis reparaciones",
                    subtitle = "Estado en tiempo real",
                    onClick = { navController.navigate(Screen.Reparaciones.route) }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DirectionsCar,
                    title = "Mis vehículos",
                    subtitle = "Gestiona tu flota",
                    onClick = { navController.navigate(Screen.Vehiculos.route)}
                )
                QuickCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    title = "Puntos CRM",
                    subtitle = "Canjea tus recompensas",
                    onClick = { navController.navigate(Screen.Crm.route) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Próxima cita (placeholder)
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
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("No tienes citas programadas", fontWeight = FontWeight.Medium)
                        Text(
                            "Pulsa 'Pedir cita' para agendar una visita",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

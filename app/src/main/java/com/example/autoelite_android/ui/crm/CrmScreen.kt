package com.example.autoelite_android.ui.crm

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
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.util.SessionManager

data class RecompensaUi(val titulo: String, val puntos: Int, val descripcion: String)

private val recompensas = listOf(
    RecompensaUi("10% de descuento",   500,  "En tu próxima reparación"),
    RecompensaUi("Revisión gratuita", 1000,  "Revisión completa sin coste"),
    RecompensaUi("Cambio de aceite",  1500,  "Mano de obra incluida"),
    RecompensaUi("25% de descuento",  2000,  "En cualquier servicio"),
)

private fun nivelPorPuntos(puntos: Int) = when {
    puntos >= 2000 -> "Gold 🥇"
    puntos >= 1000 -> "Silver 🥈"
    else           -> "Bronze 🥉"
}

private fun puntosProximoNivel(puntos: Int) = when {
    puntos >= 2000 -> "Has alcanzado el nivel máximo"
    puntos >= 1000 -> "Gold — faltan ${2000 - puntos} puntos"
    else           -> "Silver — faltan ${1000 - puntos} puntos"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(
    navController: NavController,
    viewModel: CrmViewModel = viewModel()
) {
    val cliente by viewModel.cliente.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Usamos datos del cliente o del SessionManager como fallback
    val puntos = cliente?.puntosAcumulados ?: SessionManager.puntos
    val nivel  = nivelPorPuntos(puntos)
    val proximo = puntosProximoNivel(puntos)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis puntos") }) },
        bottomBar = { AutoEliteBottomBar(navController) }
    ) { paddingValues ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Tarjeta de puntos
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Star, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("$puntos pts", fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary)
                            Text("Nivel: $nivel", fontWeight = FontWeight.SemiBold)
                            Text(proximo, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (puntos % 1000) / 1000f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Text("Recompensas disponibles",
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                items(recompensas) { recompensa ->
                    val canCanjear = puntos >= recompensa.puntos
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CardGiftcard, null,
                                tint = if (canCanjear) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(recompensa.titulo, fontWeight = FontWeight.SemiBold)
                                Text(recompensa.descripcion, fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${recompensa.puntos} pts",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary)
                                if (canCanjear) {
                                    TextButton(
                                        onClick = { /* TODO */ },
                                        contentPadding = PaddingValues(0.dp)
                                    ) { Text("Canjear", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
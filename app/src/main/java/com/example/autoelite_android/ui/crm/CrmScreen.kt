package com.example.autoelite_android.ui.crm

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

data class RecompensaUi(val titulo: String, val puntos: Int, val descripcion: String)

private val recompensas = listOf(
    RecompensaUi("10% de descuento",     500,  "En tu próxima reparación"),
    RecompensaUi("Revisión gratuita",   1000, "Revisión completa sin coste"),
    RecompensaUi("Cambio de aceite",    1500, "Mano de obra incluida"),
    RecompensaUi("25% de descuento",    2000, "En cualquier servicio"),
)

// Datos de ejemplo del cliente
private const val PUNTOS_ACTUALES = 750
private const val NIVEL = "Silver" //buscar icono de medalla!!!!
private const val PROXIMO_NIVEL = "Gold — faltan 250 puntos"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis puntos") }) },
        bottomBar = { AutoEliteBottomBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
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
                        Icon(Icons.Default.Star, contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("$PUNTOS_ACTUALES pts", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary)
                        Text("Nivel: $NIVEL", fontWeight = FontWeight.SemiBold)
                        Text(PROXIMO_NIVEL, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { PUNTOS_ACTUALES / 1000f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Text("Recompensas disponibles", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            items(recompensas) { recompensa ->
                val canCanjear = PUNTOS_ACTUALES >= recompensa.puntos
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null,
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
                            Text("${recompensa.puntos} pts", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            if (canCanjear) {
                                TextButton(onClick = { /* TODO: canjear */ },
                                    contentPadding = PaddingValues(0.dp)) {
                                    Text("Canjear", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

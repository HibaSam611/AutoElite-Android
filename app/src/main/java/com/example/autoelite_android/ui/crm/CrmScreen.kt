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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.util.SessionManager

data class RecompensaUi(
    val titulo: String,
    val puntos: Int,
    val descripcion: String,
    val icono: @Composable () -> Unit = {
        Icon(Icons.Default.CardGiftcard, null)
    }
)

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
    val cliente   by viewModel.cliente.collectAsState()
    val loading   by viewModel.loading.collectAsState()
    val canjeando by viewModel.canjeando.collectAsState()
    val mensaje   by viewModel.mensaje.collectAsState()
    val error     by viewModel.error.collectAsState()

    // Dialogo de confirmación de canjeo
    var recompensaACanjear by remember { mutableStateOf<RecompensaUi?>(null) }

    // Usamos datos del cliente o del SessionManager como fallback
    val puntos  = cliente?.puntosAcumulados ?: SessionManager.puntos
    val nivel   = nivelPorPuntos(puntos)
    val proximo = puntosProximoNivel(puntos)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetMensaje()
        }
    }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis puntos") }) },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                                progress = {
                                    if (puntos >= 2000) 1f
                                    else (puntos % 1000) / 1000f
                                },
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
                    RecompensaCard(
                        recompensa = recompensa,
                        canCanjear = canCanjear,
                        canjeando = canjeando,
                        onCanjear = { recompensaACanjear = recompensa }
                    )
                }

                // Info sobre puntos
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Acumulas puntos con cada reparación. " +
                                        "1 € gastado = 1 punto acumulado.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de canjeo
    recompensaACanjear?.let { recompensa ->
        CanjearConfirmDialog(
            recompensa = recompensa,
            puntosActuales = puntos,
            canjeando = canjeando,
            onConfirmar = {
                viewModel.canjearRecompensa(
                    nombreRecompensa = recompensa.titulo,
                    puntosRequeridos = recompensa.puntos
                )
                recompensaACanjear = null
            },
            onDismiss = { recompensaACanjear = null }
        )
    }
}

// Card de recompensa
@Composable
private fun RecompensaCard(
    recompensa: RecompensaUi,
    canCanjear: Boolean,
    canjeando: Boolean,
    onCanjear: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CardGiftcard, null,
                tint = if (canCanjear) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp)
            )
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
                        onClick = onCanjear,
                        enabled = !canjeando,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (canjeando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Canjear", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Diálogo de confirmación
@Composable
private fun CanjearConfirmDialog(
    recompensa: RecompensaUi,
    puntosActuales: Int,
    canjeando: Boolean,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!canjeando) onDismiss() },
        icon = {
            Icon(
                Icons.Default.CardGiftcard, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                "Canjear recompensa",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    recompensa.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    recompensa.descripcion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Resumen de puntos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tus puntos actuales:", fontSize = 13.sp)
                    Text("$puntosActuales pts", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Coste de la recompensa:", fontSize = 13.sp)
                    Text(
                        "- ${recompensa.puntos} pts",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Puntos restantes:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "${puntosActuales - recompensa.puntos} pts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    "Recibirás un código por email para usar en tu próxima visita.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !canjeando
            ) {
                if (canjeando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Canjeando…")
                } else {
                    Icon(Icons.Default.Redeem, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirmar canjeo")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !canjeando
            ) {
                Text("Cancelar")
            }
        }
    )
}
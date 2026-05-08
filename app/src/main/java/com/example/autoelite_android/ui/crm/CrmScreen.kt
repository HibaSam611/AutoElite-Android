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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.R
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.util.SessionManager

data class RecompensaUi(
    val tituloResId: Int,
    val puntos: Int,
    val descripcionResId: Int,
    val icono: @Composable () -> Unit = {
        Icon(Icons.Default.CardGiftcard, null)
    }
)

private val recompensas = listOf(
    RecompensaUi(R.string.crm_reward_1_title, 500,  R.string.crm_reward_1_desc),
    RecompensaUi(R.string.crm_reward_2_title, 1000, R.string.crm_reward_2_desc),
    RecompensaUi(R.string.crm_reward_3_title, 1500, R.string.crm_reward_3_desc),
    RecompensaUi(R.string.crm_reward_4_title, 2000, R.string.crm_reward_4_desc),
)

@Composable
private fun nivelPorPuntos(puntos: Int) = when {
    puntos >= 2000 -> stringResource(R.string.crm_level_gold)
    puntos >= 1000 -> stringResource(R.string.crm_level_silver)
    else           -> stringResource(R.string.crm_level_bronze)
}

@Composable
private fun puntosProximoNivel(puntos: Int) = when {
    puntos >= 2000 -> stringResource(R.string.crm_max_level)
    puntos >= 1000 -> stringResource(R.string.crm_next_gold, 2000 - puntos)
    else           -> stringResource(R.string.crm_next_silver, 1000 - puntos)
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

    var recompensaACanjear by remember { mutableStateOf<RecompensaUi?>(null) }

    val puntos  = cliente?.puntosAcumulados ?: SessionManager.puntos
    val nivel   = nivelPorPuntos(puntos)
    val proximo = puntosProximoNivel(puntos)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) { mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.resetMensaje() } }
    LaunchedEffect(error) { error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() } }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.crm_title)) }) },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Star, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.crm_points, puntos), fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.crm_level, nivel), fontWeight = FontWeight.SemiBold)
                            Text(proximo, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { if (puntos >= 2000) 1f else (puntos % 1000) / 1000f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Text(stringResource(R.string.crm_rewards_title),
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

                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.crm_info),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    recompensaACanjear?.let { recompensa ->
        CanjearConfirmDialog(
            recompensa = recompensa,
            puntosActuales = puntos,
            canjeando = canjeando,
            onConfirmar = {
                viewModel.canjearRecompensa(
                    nombreRecompensa = stringResource(recompensa.tituloResId),
                    puntosRequeridos = recompensa.puntos
                )
                recompensaACanjear = null
            },
            onDismiss = { recompensaACanjear = null }
        )
    }
}

@Composable
private fun RecompensaCard(
    recompensa: RecompensaUi, canCanjear: Boolean,
    canjeando: Boolean, onCanjear: () -> Unit
) {
    val titulo = stringResource(recompensa.tituloResId)
    val descripcion = stringResource(recompensa.descripcionResId)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CardGiftcard, null,
                tint = if (canCanjear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(descripcion, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.crm_points, recompensa.puntos),
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (canCanjear) {
                    TextButton(onClick = onCanjear, enabled = !canjeando,
                        contentPadding = PaddingValues(0.dp)) {
                        if (canjeando) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.crm_redeem), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CanjearConfirmDialog(
    recompensa: RecompensaUi, puntosActuales: Int,
    canjeando: Boolean, onConfirmar: @Composable () -> Unit, onDismiss: () -> Unit
) {
    val titulo = stringResource(recompensa.tituloResId)
    val descripcion = stringResource(recompensa.descripcionResId)

    AlertDialog(
        onDismissRequest = { if (!canjeando) onDismiss() },
        icon = {
            Icon(Icons.Default.CardGiftcard, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp))
        },
        title = { Text(stringResource(R.string.crm_redeem_title), textAlign = TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                Text(descripcion, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.crm_current_points), fontSize = 13.sp)
                    Text(stringResource(R.string.crm_points, puntosActuales),
                        fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.crm_reward_cost), fontSize = 13.sp)
                    Text("- ${stringResource(R.string.crm_points, recompensa.puntos)}",
                        fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error)
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.crm_remaining_points),
                        fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(stringResource(R.string.crm_points, puntosActuales - recompensa.puntos),
                        fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
                Text(stringResource(R.string.crm_email_code),
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(onClick = onConfirmar as () -> Unit, enabled = !canjeando) {
                if (canjeando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.crm_redeeming))
                } else {
                    Icon(Icons.Default.Redeem, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.crm_confirm_redeem))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !canjeando) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
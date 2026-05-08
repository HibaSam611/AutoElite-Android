package com.example.autoelite_android.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.R
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController,
    viewModel: NotificacionesViewModel = viewModel()
) {
    val notificaciones by viewModel.notificaciones.collectAsState()
    val noLeidas       by viewModel.noLeidas.collectAsState()
    val loading        by viewModel.loading.collectAsState()
    val error          by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.resetError() }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarNotificaciones()
        viewModel.cargarContadorNoLeidas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notif_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (noLeidas > 0) {
                        TextButton(onClick = { viewModel.marcarTodasLeidas() }) {
                            Text(stringResource(R.string.notif_mark_all_read))
                        }
                    }
                }
            )
        },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            loading -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            notificaciones.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.notif_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.notif_empty_subtitle),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp))
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(notificaciones) { notif ->
                    NotificacionItemRow(
                        notificacion = notif,
                        onClick = {
                            if (!notif.leida) viewModel.marcarLeida(notif.id)
                            val ruta = when (notif.pantalla) {
                                "citas"        -> Screen.Citas.route
                                "reparaciones" -> Screen.Reparaciones.route
                                "facturacion"  -> Screen.Facturacion.route
                                else           -> null
                            }
                            ruta?.let {
                                navController.navigate(it) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificacionItemRow(
    notificacion: NotificacionItem,
    onClick: () -> Unit
) {
    val bgColor = if (!notificacion.leida)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    else MaterialTheme.colorScheme.surface

    val icono = when {
        notificacion.pantalla == "citas" -> Icons.Default.CalendarMonth
        notificacion.pantalla == "reparaciones" -> Icons.Default.Build
        notificacion.pantalla == "facturacion" -> Icons.Default.Receipt
        else -> Icons.Default.Notifications
    }

    val iconColor = when {
        notificacion.pantalla == "citas" -> MaterialTheme.colorScheme.primary
        notificacion.pantalla == "reparaciones" -> MaterialTheme.colorScheme.tertiary
        notificacion.pantalla == "facturacion" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (!notificacion.leida) {
            Box(modifier = Modifier
                .padding(top = 6.dp, end = 8.dp)
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraLarge))
        } else {
            Spacer(Modifier.width(16.dp))
        }

        Surface(modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.medium,
            color = iconColor.copy(alpha = 0.1f)) {
            Icon(icono, null, tint = iconColor, modifier = Modifier.padding(8.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(notificacion.titulo,
                fontWeight = if (!notificacion.leida) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(notificacion.cuerpo, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(notificacion.fecha, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline)
        }

        if (notificacion.pantalla.isNotBlank()) {
            Icon(Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterVertically).size(20.dp))
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

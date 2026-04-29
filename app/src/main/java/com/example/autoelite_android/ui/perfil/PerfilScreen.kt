package com.example.autoelite_android.ui.perfil

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.ui.auth.AuthViewModel
import com.example.autoelite_android.ui.theme.ThemeViewModel
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    var showLogoutDialog   by remember { mutableStateOf(false) }
    var showEditDialog     by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showNotifDialog    by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showContactDialog  by remember { mutableStateOf(false) }
    var showAboutDialog    by remember { mutableStateOf(false) }

    var nombre    by remember { mutableStateOf(SessionManager.nombre) }
    var apellidos by remember { mutableStateOf(SessionManager.apellidos) }
    val email     = user?.email ?: SessionManager.email.ifBlank { "" }
    val puntos    = SessionManager.puntos

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi perfil") }) },
        bottomBar = { AutoEliteBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.size(96.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.padding(16.dp), tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(12.dp))
            Text("$nombre $apellidos".trim().ifBlank { "Usuario" }, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)

            Spacer(Modifier.height(8.dp))
            SuggestionChip(onClick = {}, label = { Text("$puntos puntos acumulados", fontSize = 13.sp) })

            Spacer(Modifier.height(24.dp))

            // ── Apariencia ──
            ProfileSection(title = "Apariencia") {
                // Toggle modo oscuro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        null, tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Modo oscuro",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { themeViewModel.toggleTheme() },
                        thumbContent = {
                            Icon(
                                if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Cuenta ──
            ProfileSection(title = "Cuenta") {
                ProfileRow(Icons.Outlined.Edit, "Editar datos personales") { showEditDialog = true }
                ProfileRow(Icons.Outlined.Lock, "Cambiar contraseña") { showPasswordDialog = true }
                ProfileRow(Icons.Outlined.Notifications, "Notificaciones") { showNotifDialog = true }
            }

            Spacer(Modifier.height(12.dp))

            // ── Soporte ──
            ProfileSection(title = "Soporte") {
                ProfileRow(Icons.Outlined.LocationOn, "Localización del taller") { showLocationDialog = true }
                ProfileRow(Icons.Outlined.Phone, "Contactar con el taller") { showContactDialog = true }
                ProfileRow(Icons.Outlined.Info, "Acerca de AutoElite") { showAboutDialog = true }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── DIÁLOGOS ──
    if (showLogoutDialog) {
        AlertDialog(onDismissRequest = { showLogoutDialog = false }, title = { Text("¿Cerrar sesión?") },
            text = { Text("Se cerrará tu sesión en este dispositivo.") },
            confirmButton = { Button(onClick = { authViewModel.logout(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } })
    }

    if (showEditDialog) {
        EditarDatosDialog(nombre, apellidos, SessionManager.telefono,
            onDismiss = { showEditDialog = false },
            onGuardar = { n, a, t ->
                authViewModel.updateProfile(n, a, t,
                    onSuccess = { nombre = n; apellidos = a; showEditDialog = false },
                    onError = { showEditDialog = false })
            })
    }

    if (showPasswordDialog) {
        CambiarPasswordDialog(onDismiss = { showPasswordDialog = false },
            onCambiar = { cur, nueva ->
                authViewModel.changePassword(cur, nueva,
                    onSuccess = { showPasswordDialog = false }, onError = {})
            })
    }

    if (showNotifDialog) NotificacionesDialog(onDismiss = { showNotifDialog = false })
    if (showLocationDialog) LocalizacionDialog(onDismiss = { showLocationDialog = false })
    if (showContactDialog) ContactarDialog(onDismiss = { showContactDialog = false })
    if (showAboutDialog) AcercaDeDialog(onDismiss = { showAboutDialog = false })
}

// ── Componentes ──

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(content = content)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
    }
}

// ── Diálogos ──

@Composable
private fun EditarDatosDialog(nombreActual: String, apellidosActual: String, telefonoActual: String, onDismiss: () -> Unit, onGuardar: (String, String, String) -> Unit) {
    var nombre by remember { mutableStateOf(nombreActual) }; var apellidos by remember { mutableStateOf(apellidosActual) }; var telefono by remember { mutableStateOf(telefonoActual) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Editar datos") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(apellidos, { apellidos = it }, label = { Text("Apellidos") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { Button(onClick = { onGuardar(nombre, apellidos, telefono) }, enabled = nombre.isNotBlank() && apellidos.isNotBlank()) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun CambiarPasswordDialog(onDismiss: () -> Unit, onCambiar: (String, String) -> Unit) {
    var actual by remember { mutableStateOf("") }; var nueva by remember { mutableStateOf("") }; var confirmar by remember { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Cambiar contraseña") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(actual, { actual = it }, label = { Text("Contraseña actual") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(nueva, { nueva = it }, label = { Text("Nueva contraseña") }, leadingIcon = { Icon(Icons.Default.LockReset, null) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(confirmar, { confirmar = it }, label = { Text("Confirmar nueva") }, leadingIcon = { Icon(Icons.Default.LockReset, null) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), isError = confirmar.isNotEmpty() && nueva != confirmar)
            if (confirmar.isNotEmpty() && nueva != confirmar) Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        } },
        confirmButton = { Button(onClick = { when { actual.isBlank() || nueva.isBlank() -> error = "Rellena todos los campos"; nueva.length < 6 -> error = "Mínimo 6 caracteres"; nueva != confirmar -> error = "No coinciden"; else -> onCambiar(actual, nueva) } }) { Text("Cambiar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun NotificacionesDialog(onDismiss: () -> Unit) {
    var notifCitas by remember { mutableStateOf(SessionManager.notifCitas) }; var notifRep by remember { mutableStateOf(SessionManager.notifReparaciones) }; var notifPromo by remember { mutableStateOf(SessionManager.notifPromociones) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Notificaciones") },
        text = { Column {
            NotifRow("Citas y recordatorios", notifCitas) { notifCitas = it; SessionManager.notifCitas = it }
            NotifRow("Estado de reparaciones", notifRep) { notifRep = it; SessionManager.notifReparaciones = it }
            NotifRow("Promociones y ofertas", notifPromo) { notifPromo = it; SessionManager.notifPromociones = it }
            Spacer(Modifier.height(8.dp)); Text("Se guardan automáticamente", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}

@Composable private fun NotifRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp); Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun LocalizacionDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Localización") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)); Spacer(Modifier.width(8.dp))
                Column { Text("AutoElite Taller", fontWeight = FontWeight.SemiBold); Text("Calle de Ejemplo 42, 28001 Madrid", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Lun-Vie: 8:00–18:00\nSáb: 9:00–14:00", fontSize = 13.sp)
            }
            Button(onClick = { val uri = Uri.parse("geo:40.4168,-3.7038?q=${Uri.encode("Calle de Ejemplo 42, Madrid")}"); context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Map, null); Spacer(Modifier.width(8.dp)); Text("Abrir en Maps") }
        } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}

@Composable
private fun ContactarDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Contactar") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+34912345678"))) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Phone, null); Spacer(Modifier.width(8.dp)); Text("Llamar: +34 912 345 678") }
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:info@autoelite.es"); putExtra(Intent.EXTRA_SUBJECT, "Consulta AutoElite App") }) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Email, null); Spacer(Modifier.width(8.dp)); Text("Email: info@autoelite.es") }
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/34912345678"))) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Chat, null); Spacer(Modifier.width(8.dp)); Text("WhatsApp") }
        } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}

@Composable
private fun AcercaDeDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Acerca de AutoElite") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AutoElite", fontWeight = FontWeight.Bold, fontSize = 20.sp); Text("Gestión integral de talleres", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            InfoRow("Versión", "1.0.0"); InfoRow("Desarrollador", "TFG - AutoElite"); InfoRow("Plataforma", "Android (Compose)"); InfoRow("Backend", "Spring Boot + Firebase")
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Aplicación desarrollada como TFG para gestión centralizada de citas, vehículos, reparaciones y facturación.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}

@Composable private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) { Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(100.dp)); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
}
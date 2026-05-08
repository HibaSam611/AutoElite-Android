package com.example.autoelite_android.ui.perfil

import android.app.Activity
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.R
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.ui.auth.AuthViewModel
import com.example.autoelite_android.ui.theme.ThemeViewModel
import com.example.autoelite_android.util.LocaleManager
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
    var showLanguageDialog by remember { mutableStateOf(false) }

    var nombre    by remember { mutableStateOf(SessionManager.nombre) }
    var apellidos by remember { mutableStateOf(SessionManager.apellidos) }
    val email     = user?.email ?: SessionManager.email.ifBlank { "" }
    val puntos    = SessionManager.puntos

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.perfil_title)) }) },
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
                Icon(
                    Icons.Default.Person,
                    null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "$nombre $apellidos".trim().ifBlank { stringResource(R.string.perfil_user_default) },
                fontWeight = FontWeight.Bold, fontSize = 22.sp
            )
            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)

            Spacer(Modifier.height(8.dp))
            SuggestionChip(
                onClick = {},
                label = { Text(stringResource(R.string.perfil_points, puntos), fontSize = 13.sp) }
            )

            Spacer(Modifier.height(24.dp))

            // Apariencia
            ProfileSection(title = stringResource(R.string.perfil_appearance)) {
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
                        stringResource(R.string.perfil_dark_mode),
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

                // Selector de idioma
                ProfileRow(
                    Icons.Outlined.Language,
                    "${stringResource(R.string.perfil_language)}: ${LocaleManager.currentLanguageLabel()}"
                ) { showLanguageDialog = true }
            }

            Spacer(Modifier.height(12.dp))

            // Cuenta
            ProfileSection(title = stringResource(R.string.perfil_account)) {
                ProfileRow(Icons.Outlined.Edit, stringResource(R.string.perfil_edit_data)) { showEditDialog = true }
                ProfileRow(Icons.Outlined.Lock, stringResource(R.string.perfil_change_password)) { showPasswordDialog = true }
                ProfileRow(Icons.Outlined.Notifications, stringResource(R.string.perfil_notifications)) { showNotifDialog = true }
            }

            Spacer(Modifier.height(12.dp))

            // Soporte
            ProfileSection(title = stringResource(R.string.perfil_support)) {
                ProfileRow(Icons.Outlined.LocationOn, stringResource(R.string.perfil_location)) { showLocationDialog = true }
                ProfileRow(Icons.Outlined.Phone, stringResource(R.string.perfil_contact)) { showContactDialog = true }
                ProfileRow(Icons.Outlined.Info, stringResource(R.string.perfil_about)) { showAboutDialog = true }
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
                Text(stringResource(R.string.perfil_logout))
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // DIÁLOGOS
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.perfil_logout_title)) },
            text = { Text(stringResource(R.string.perfil_logout_message)) },
            confirmButton = {
                Button(
                    onClick = { authViewModel.logout(); onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.perfil_logout_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
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

    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { langCode ->
                showLanguageDialog = false
                LocaleManager.setLocale(navController.context, langCode)
                // Recrear la Activity para aplicar el nuevo idioma
                (navController.context as? Activity)?.recreate()
            }
        )
    }
}

// Componentes

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(content = content)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
    }
}

// Diálogo de idioma

@Composable
private fun LanguageDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val currentLang = SessionManager.language.ifBlank { LocaleManager.SPANISH }
    val languages = LocaleManager.availableLanguages()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Language, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text(stringResource(R.string.language_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                languages.forEach { (code, name) ->
                    val isSelected = code == currentLang
                    Surface(
                        onClick = {
                            if (!isSelected) onLanguageSelected(code)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bandera/emoji
                            Text(
                                when (code) {
                                    "es" -> "🇪🇸"
                                    "en" -> "🇬🇧"
                                    "ar" -> "🇸🇦"
                                    else -> "🌐"
                                },
                                fontSize = 24.sp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.language_restart_message),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}



@Composable
private fun EditarDatosDialog(nombreActual: String, apellidosActual: String, telefonoActual: String, onDismiss: () -> Unit, onGuardar: (String, String, String) -> Unit) {
    var nombre by remember { mutableStateOf(nombreActual) };
    var apellidos by remember { mutableStateOf(apellidosActual) };
    var telefono by remember { mutableStateOf(telefonoActual) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                nombre, { nombre = it },
                label = { Text(stringResource(R.string.register_name)) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                apellidos, { apellidos = it },
                label = { Text(stringResource(R.string.register_surname)) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                telefono, { telefono = it },
                label = { Text(stringResource(R.string.edit_phone)) },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            ) }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, apellidos, telefono) },
                enabled = nombre.isNotBlank() && apellidos.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss)
        { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun CambiarPasswordDialog(onDismiss: () -> Unit, onCambiar: (String, String) -> Unit) {
    var actual by remember { mutableStateOf("") };
    var nueva by remember { mutableStateOf("") };
    var confirmar by remember { mutableStateOf("") };
    var error by remember { mutableStateOf<String?>(null) }
    val errorFill = stringResource(R.string.password_fill_all)
    val errorMin = stringResource(R.string.password_min_length)
    val errorMatch = stringResource(R.string.password_no_match)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.perfil_change_password)) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                actual, { actual = it },
                label = { Text(stringResource(R.string.password_current)) },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                nueva, { nueva = it },
                label = { Text(stringResource(R.string.password_new)) },
                leadingIcon = { Icon(Icons.Default.LockReset, null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                confirmar,
                { confirmar = it },
                label = { Text(stringResource(R.string.password_confirm_new)) },
                leadingIcon = { Icon(Icons.Default.LockReset, null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                isError = confirmar.isNotEmpty() && nueva != confirmar
            )
            if (confirmar.isNotEmpty() && nueva != confirmar) Text(stringResource(R.string.password_mismatch),
                color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        } },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        actual.isBlank() || nueva.isBlank() -> error = errorFill; nueva.length < 6 -> error = errorMin; nueva != confirmar -> error = errorMatch
                        else -> onCambiar(actual, nueva)
                    }
                }
            ) {
                Text(stringResource(R.string.password_change))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun NotificacionesDialog(onDismiss: () -> Unit) {
    var notifCitas by remember { mutableStateOf(SessionManager.notifCitas) }
    var notifRep by remember { mutableStateOf(SessionManager.notifReparaciones) }
    var notifPromo by remember { mutableStateOf(SessionManager.notifPromociones) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.perfil_notifications)) },
        text = {
            Column {
                NotifRow(stringResource(R.string.notif_config_citas), notifCitas) {
                    notifCitas = it;
                    SessionManager.notifCitas = it
                }
                NotifRow(stringResource(R.string.notif_config_reparaciones), notifRep) {
                    notifRep = it;
                    SessionManager.notifReparaciones = it
                }
                NotifRow(stringResource(R.string.notif_config_promos), notifPromo) {
                    notifPromo = it;
                    SessionManager.notifPromociones = it
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.notif_config_autosave),
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } })
}

@Composable private fun NotifRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun LocalizacionDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.location_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                Column {
                    Text(stringResource(R.string.location_name), fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.location_address),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.location_hours),fontSize = 13.sp)
            }
            Button(
                onClick = { val uri = Uri.parse("geo:40.4168,-3.7038?q=${Uri.encode("Avenida de las Vascongadas , Getafe")}");
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
                modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Map, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.location_open_maps)) }
        } },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        }
    )
}

@Composable
private fun ContactarDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.contact_title)) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+34912345678"))) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Phone, null);
                Spacer(Modifier.width(8.dp));
                Text(stringResource(R.string.contact_phone))
            }
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:info@autoelite.es")
                    putExtra(Intent.EXTRA_SUBJECT, "Consulta AutoElite App") })
                },
                modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Email, null);
                Spacer(Modifier.width(8.dp));
                Text(stringResource(R.string.contact_email)) }
            OutlinedButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/34912345678"))) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Chat, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.contact_whatsapp))
            }
        } },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } })
}

@Composable
private fun AcercaDeDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.about_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AutoElite", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(stringResource(R.string.app_tagline_full), color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            InfoRow(stringResource(R.string.about_version), stringResource(R.string.about_version_value))
            InfoRow(stringResource(R.string.about_developer), stringResource(R.string.about_developer_value))
            InfoRow(stringResource(R.string.about_platform), stringResource(R.string.about_platform_value))
            InfoRow(stringResource(R.string.about_backend), stringResource(R.string.about_backend_value))
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.about_description), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } })
}

@Composable private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

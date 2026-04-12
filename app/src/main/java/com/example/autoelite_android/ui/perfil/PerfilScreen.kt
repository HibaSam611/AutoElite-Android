package com.example.autoelite_android.ui.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autoelite_android.navigation.AutoEliteBottomBar
import com.example.autoelite_android.ui.auth.AuthViewModel
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }

    val nombre = user?.displayName ?: SessionManager.nombre.ifBlank { "Usuario" }
    val email  = user?.email ?: SessionManager.email.ifBlank { "" }
    val puntos = SessionManager.puntos

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi perfil") }) },
        bottomBar = { AutoEliteBottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Avatar
            Surface(
                modifier = Modifier.size(96.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Person, null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(12.dp))
            Text(nombre, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)

            Spacer(Modifier.height(8.dp))

            // Chip de puntos
            SuggestionChip(
                onClick = {},
                label = { Text("⭐ $puntos puntos acumulados", fontSize = 13.sp) }
            )

            Spacer(Modifier.height(24.dp))

            ProfileSection(title = "Cuenta") {
                ProfileRow(Icons.Default.Edit, "Editar datos personales") { }
                ProfileRow(Icons.Default.Lock, "Cambiar contraseña") { }
                ProfileRow(Icons.Default.Notifications, "Notificaciones") { }
            }

            Spacer(Modifier.height(12.dp))

            ProfileSection(title = "Soporte") {
                ProfileRow(Icons.Default.LocationOn, "Localización del taller") { }
                ProfileRow(Icons.Default.Phone, "Contactar con el taller") { }
                ProfileRow(Icons.Default.Info, "Acerca de AutoElite") { }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Se cerrará tu sesión en este dispositivo.") },
            confirmButton = {
                Button(
                    onClick = { authViewModel.logout(); onLogout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
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
        Text(label, color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null,
            tint = MaterialTheme.colorScheme.outline)
    }
}
package com.example.autoelite_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.autoelite_android.navigation.Screen
import com.example.autoelite_android.navigation.NavGraph
import com.example.autoelite_android.notifications.AutoEliteMessagingService
import com.example.autoelite_android.ui.theme.AutoEliteAndroidTheme
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    // Lanzador para pedir permiso de notificaciones (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            println("Permiso de notificaciones concedido")
            registrarTokenFcm()
        } else {
            println("Permiso de notificaciones denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar SessionManager con DataStore
        SessionManager.init(applicationContext)

        // Crear canal de notificaciones
        AutoEliteMessagingService.crearCanal(this)

        // Pedir permiso de notificaciones (Android 13+)
        pedirPermisoNotificaciones()

        setContent {
            AutoEliteAndroidTheme {
                val navController = rememberNavController()

                val startDestination = if (
                    FirebaseAuth.getInstance().currentUser != null &&
                    SessionManager.clienteId != -1L
                ) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Ya tenemos permiso
                    registrarTokenFcm()
                }
                else -> {
                    // Pedir permiso
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        } else {
            // Android < 13, no necesita permiso explícito
            registrarTokenFcm()
        }
    }

     //Obtiene el token FCM actual y lo envía al backend (solo si hay un usuario logueado)
    private fun registrarTokenFcm() {
        if (FirebaseAuth.getInstance().currentUser == null) return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                println("FCM Token: ${token.take(20)}...")
                AutoEliteMessagingService.enviarTokenAlBackend(token)
            }
            .addOnFailureListener { e ->
                println("Error obteniendo FCM token: ${e.message}")
            }
    }
}
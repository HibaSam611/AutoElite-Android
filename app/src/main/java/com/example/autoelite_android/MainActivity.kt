package com.example.autoelite_android

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.autoelite_android.navigation.Screen
import com.example.autoelite_android.navigation.NavGraph
import com.example.autoelite_android.notifications.AutoEliteMessagingService
import com.example.autoelite_android.ui.theme.AutoEliteAndroidTheme
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private var isReady = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            registrarTokenFcm()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ── Splash Screen (ANTES de super.onCreate) ──
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar SessionManager
        SessionManager.init(applicationContext)

        // Crear canal de notificaciones
        AutoEliteMessagingService.crearCanal(this)

        // Pedir permiso de notificaciones
        pedirPermisoNotificaciones()

        // Mantener splash hasta que estemos listos
        splashScreen.setKeepOnScreenCondition { !isReady }

        // Animación de salida del splash (zoom out con rebote)
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val scaleX = ObjectAnimator.ofFloat(
                splashScreenView.iconView, View.SCALE_X, 1f, 0f
            ).apply {
                interpolator = OvershootInterpolator()
                duration = 400L
            }
            val scaleY = ObjectAnimator.ofFloat(
                splashScreenView.iconView, View.SCALE_Y, 1f, 0f
            ).apply {
                interpolator = OvershootInterpolator()
                duration = 400L
            }
            val alpha = ObjectAnimator.ofFloat(
                splashScreenView.view, View.ALPHA, 1f, 0f
            ).apply {
                duration = 400L
                doOnEnd { splashScreenView.remove() }
            }

            scaleX.start()
            scaleY.start()
            alpha.start()
        }

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

                // Marcar como listo para que el splash se vaya
                isReady = true

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
                    registrarTokenFcm()
                }
                else -> {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        } else {
            registrarTokenFcm()
        }
    }

    private fun registrarTokenFcm() {
        if (FirebaseAuth.getInstance().currentUser == null) return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                AutoEliteMessagingService.enviarTokenAlBackend(token)
            }
            .addOnFailureListener { e ->
                println("Error obteniendo FCM token: ${e.message}")
            }
    }
}
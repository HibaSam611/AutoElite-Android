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
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.autoelite_android.navigation.Screen
import com.example.autoelite_android.navigation.NavGraph
import com.example.autoelite_android.notifications.AutoEliteMessagingService
import com.example.autoelite_android.ui.theme.AutoEliteAndroidTheme
import com.example.autoelite_android.ui.theme.ThemeViewModel
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private var isReady = false
    private val themeViewModel: ThemeViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) registrarTokenFcm()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SessionManager.init(applicationContext)
        AutoEliteMessagingService.crearCanal(this)
        pedirPermisoNotificaciones()

        splashScreen.setKeepOnScreenCondition { !isReady }
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val scaleX = ObjectAnimator.ofFloat(splashScreenView.iconView, View.SCALE_X, 1f, 0f).apply {
                interpolator = OvershootInterpolator(); duration = 400L
            }
            val scaleY = ObjectAnimator.ofFloat(splashScreenView.iconView, View.SCALE_Y, 1f, 0f).apply {
                interpolator = OvershootInterpolator(); duration = 400L
            }
            val alpha = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f).apply {
                duration = 400L; doOnEnd { splashScreenView.remove() }
            }
            scaleX.start(); scaleY.start(); alpha.start()
        }

        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            AutoEliteAndroidTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                val startDestination = if (
                    FirebaseAuth.getInstance().currentUser != null &&
                    SessionManager.clienteId != -1L
                ) Screen.Home.route else Screen.Login.route

                isReady = true

                NavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    themeViewModel = themeViewModel
                )
            }
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                registrarTokenFcm()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            registrarTokenFcm()
        }
    }

    private fun registrarTokenFcm() {
        if (FirebaseAuth.getInstance().currentUser == null) return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { AutoEliteMessagingService.enviarTokenAlBackend(it) }
            .addOnFailureListener { println("Error FCM token: ${it.message}") }
    }
}
package com.example.autoelite_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.autoelite_android.navigation.Screen
import com.example.autoelite_android.navigation.NavGraph
import com.example.autoelite_android.ui.theme.AutoEliteAndroidTheme
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar SessionManager con DataStore ANTES de todo
        SessionManager.init(applicationContext)

        setContent {
            AutoEliteAndroidTheme {
                val navController = rememberNavController()

                // Si ya hay sesión activa en Firebase Y tenemos clienteId guardado
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
}
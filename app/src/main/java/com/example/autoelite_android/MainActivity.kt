package com.example.autoelite_android
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.autoelite_android.navigation.Screen
import com.example.autoelite_android.navigation.NavGraph
import com.example.autoelite_android.ui.theme.AutoEliteAndroidTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoEliteAndroidTheme() {
                val navController = rememberNavController()

                // Si ya hay sesión activa, arrancamos en Home directamente
                val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
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
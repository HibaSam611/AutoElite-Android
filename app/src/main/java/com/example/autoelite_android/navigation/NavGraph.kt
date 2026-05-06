package com.example.autoelite_android.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.autoelite_android.ui.perfil.PerfilScreen
import com.example.autoelite_android.ui.auth.RegisterScreen
import com.example.autoelite_android.ui.auth.LoginScreen
import com.example.autoelite_android.ui.citas.CitasScreen
import com.example.autoelite_android.ui.crm.CrmScreen
import com.example.autoelite_android.ui.facturacion.FacturacionScreen
import com.example.autoelite_android.ui.historial.HistorialScreen
import com.example.autoelite_android.ui.home.HomeScreen
import com.example.autoelite_android.ui.notificaciones.NotificacionesScreen
import com.example.autoelite_android.ui.reparaciones.ReparacionesScreen
import com.example.autoelite_android.ui.theme.ThemeViewModel
import com.example.autoelite_android.ui.vehiculos.VehiculosScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    themeViewModel: ThemeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Animaciones por defecto
        enterTransition = { tabEnter },
        exitTransition = { tabExit },
        popEnterTransition = { tabEnter },
        popExitTransition = { tabExit }
    ) {

        composable(
            route = Screen.Login.route,
            enterTransition = { authEnter },
            exitTransition = { authExit },
            popEnterTransition = { authEnter },
            popExitTransition = { authExit }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(
            route = Screen.Register.route,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(route = Screen.Citas.route) {
            CitasScreen(navController = navController)
        }

        composable(route = Screen.Vehiculos.route) {
            VehiculosScreen(navController = navController)
        }

        composable(route = Screen.Reparaciones.route) {
            ReparacionesScreen(navController = navController)
        }

        composable(route = Screen.Perfil.route) {
            PerfilScreen(
                navController = navController,
                themeViewModel = themeViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Facturacion.route,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            FacturacionScreen(navController = navController)
        }

        composable(
            route = Screen.Crm.route,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            CrmScreen(navController = navController)
        }

        composable(
            route = Screen.Historial.route,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            HistorialScreen(navController = navController)
        }

        composable(
            route = Screen.Notificaciones.route,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            NotificacionesScreen(navController = navController)
        }
    }
}
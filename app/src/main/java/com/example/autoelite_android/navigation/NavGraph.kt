package com.example.autoelite_android.navigation

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
import com.example.autoelite_android.ui.home.HomeScreen
import com.example.autoelite_android.ui.reparaciones.ReparacionesScreen
import com.example.autoelite_android.ui.vehiculos.VehiculosScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Citas.route) {
            CitasScreen(navController = navController)
        }
        composable(Screen.Vehiculos.route) {
            VehiculosScreen(navController = navController)
        }
        composable(Screen.Reparaciones.route) {
            ReparacionesScreen(navController = navController)
        }
        composable(Screen.Facturacion.route) {
            FacturacionScreen(navController = navController)
        }
        composable(Screen.Crm.route) {
            CrmScreen(navController = navController)
        }
        composable(Screen.Perfil.route) {
            PerfilScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

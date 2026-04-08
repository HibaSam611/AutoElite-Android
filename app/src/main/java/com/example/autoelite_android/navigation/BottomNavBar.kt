package com.example.autoelite_android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.collections.forEach

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Inicio", Icons.Default.Home, Screen.Home.route),
    BottomNavItem("Citas", Icons.Default.CalendarMonth, Screen.Citas.route),
    BottomNavItem("Vehículos", Icons.Default.DirectionsCar, Screen.Vehiculos.route),
    BottomNavItem("Reparaciones", Icons.Default.Build, Screen.Reparaciones.route),
    BottomNavItem("Perfil", Icons.Default.Person, Screen.Perfil.route),
)

@Composable
fun AutoEliteBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Evita apilar la misma pantalla
                            NavOptionsBuilder.popUpTo(Screen.Home.route) {
                                PopUpToBuilder.saveState = true
                            }
                            NavOptionsBuilder.launchSingleTop = true
                            NavOptionsBuilder.restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

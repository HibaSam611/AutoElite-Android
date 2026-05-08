package com.example.autoelite_android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.autoelite_android.R

data class BottomNavItem(
    val labelResId: Int,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_home,          Icons.Default.Home,           Screen.Home.route),
    BottomNavItem(R.string.nav_citas,         Icons.Default.CalendarMonth,  Screen.Citas.route),
    BottomNavItem(R.string.nav_vehiculos,     Icons.Default.DirectionsCar,  Screen.Vehiculos.route),
    BottomNavItem(R.string.nav_reparaciones,  Icons.Default.Build,          Screen.Reparaciones.route),
    BottomNavItem(R.string.nav_perfil,        Icons.Default.Person,         Screen.Perfil.route),
)

@Composable
fun AutoEliteBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            val label = stringResource(item.labelResId)
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        if (item.route == Screen.Home.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

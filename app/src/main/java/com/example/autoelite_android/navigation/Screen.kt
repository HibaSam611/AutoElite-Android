package com.example.autoelite_android.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login    : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Citas       : Screen("citas")
    object Vehiculos   : Screen("vehiculos")
    object Reparaciones: Screen("reparaciones")
    object Facturacion : Screen("facturacion")
    object Crm         : Screen("crm")
    object Historial   : Screen("historial")
    object Perfil      : Screen("perfil")
}
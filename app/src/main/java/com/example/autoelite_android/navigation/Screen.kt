package com.example.autoelite_android.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login    : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
}
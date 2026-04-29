package com.example.autoelite_android.ui.theme

import androidx.lifecycle.ViewModel
import com.example.autoelite_android.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel : ViewModel() {

    private val _isDarkMode = MutableStateFlow(SessionManager.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun toggleTheme() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        SessionManager.isDarkMode = newValue
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        SessionManager.isDarkMode = enabled
    }
}
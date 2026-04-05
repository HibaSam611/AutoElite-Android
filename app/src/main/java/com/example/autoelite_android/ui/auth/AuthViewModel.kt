package com.example.autoelite_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estado de la UI de autenticación
sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // Email / Password
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Rellena todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun register(nombre: String, email: String, password: String, confirmPassword: String) {
        when {
            nombre.isBlank() || email.isBlank() || password.isBlank() ->
                _uiState.value = AuthUiState.Error("Rellena todos los campos")
            password != confirmPassword ->
                _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
            password.length < 6 ->
                _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres")
            else -> viewModelScope.launch {
                _uiState.value = AuthUiState.Loading
                try {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    // Actualizamos el displayName en Firebase
                    result.user?.updateProfile(
                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(nombre)
                            .build()
                    )?.await()
                    _uiState.value = AuthUiState.Success
                } catch (e: Exception) {
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al registrarse")
                }
            }
        }
    }

    // Google SignIn
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error con Google")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

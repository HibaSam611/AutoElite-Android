package com.example.autoelite_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.RegisterRequest
import com.example.autoelite_android.network.RetrofitClient
import com.example.autoelite_android.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val api  = RetrofitClient.instance

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // Login
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Rellena todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                cargarSesion()
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    // Registro
    fun register(nombre: String, apellidos: String, email: String,
                 password: String, confirmPassword: String) {
        when {
            nombre.isBlank() || email.isBlank() || password.isBlank() ->
                _uiState.value = AuthUiState.Error("Rellena todos los campos")
            password != confirmPassword ->
                _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
            password.length < 6 ->
                _uiState.value = AuthUiState.Error("Mínimo 6 caracteres")
            else -> viewModelScope.launch {
                _uiState.value = AuthUiState.Loading
                try {
                    // 1. Crear en Firebase
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val uid = result.user?.uid ?: throw Exception("UID no disponible")

                    // 2. Actualizar displayName en Firebase
                    result.user?.updateProfile(
                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName("$nombre $apellidos")
                            .build()
                    )?.await()

                    // 3. Registrar en el backend
                    val response = api.register(
                        RegisterRequest(
                            firebaseUid = uid,
                            nombre      = nombre,
                            apellidos   = apellidos,
                            email       = email,
                            rol         = "CLIENTE"
                        )
                    )

                    if (response.isSuccessful) {
                        cargarSesion()
                        _uiState.value = AuthUiState.Success
                    } else {
                        // Si falla el backend, borramos el usuario de Firebase
                        result.user?.delete()?.await()
                        _uiState.value = AuthUiState.Error("Error al registrar en el sistema")
                    }
                } catch (e: Exception) {
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al registrarse")
                }
            }
        }
    }

    // Cargar datos del cliente en sesión
    private suspend fun cargarSesion() {
        try {
            val uid = auth.currentUser?.uid ?: return
            val response = api.getMe(uid)
            if (response.isSuccessful) {
                val usuario = response.body() ?: return
                SessionManager.usuarioId = usuario.id
                SessionManager.nombre    = usuario.nombre
                SessionManager.email     = usuario.email

                // Cargar clienteId si es CLIENTE
                if (usuario.rol == "CLIENTE") {
                    val clientes = api.getClientes()
                    if (clientes.isSuccessful) {
                        val cliente = clientes.body()
                            ?.find { it.email == usuario.email }
                        cliente?.let {
                            SessionManager.clienteId = it.id
                            SessionManager.puntos    = it.puntosAcumulados
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // No bloqueamos el login si falla la carga de sesión
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                cargarSesion()
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error con Google")
            }
        }
    }

    fun logout() {
        auth.signOut()
        SessionManager.clienteId = -1L
        SessionManager.usuarioId = -1L
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
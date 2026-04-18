package com.example.autoelite_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelite_android.model.RegisterRequest
import com.example.autoelite_android.model.UpdateProfileRequest
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

    // ── Login ──
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

    // ── Registro ──
    fun register(
        nombre: String, apellidos: String, email: String,
        password: String, confirmPassword: String
    ) {
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
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val uid = result.user?.uid ?: throw Exception("UID no disponible")

                    result.user?.updateProfile(
                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName("$nombre $apellidos")
                            .build()
                    )?.await()

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
                        result.user?.delete()?.await()
                        _uiState.value = AuthUiState.Error("Error al registrar en el sistema")
                    }
                } catch (e: Exception) {
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al registrarse")
                }
            }
        }
    }

    // ── Actualizar perfil ──
    fun updateProfile(
        nombre: String,
        apellidos: String,
        telefono: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val response = api.updateProfile(
                    uid,
                    UpdateProfileRequest(nombre, apellidos, telefono.ifBlank { null })
                )
                if (response.isSuccessful) {
                    response.body()?.let { usuario ->
                        SessionManager.nombre    = usuario.nombre
                        SessionManager.apellidos = usuario.apellidos
                        SessionManager.telefono  = usuario.telefono ?: ""

                        // Actualizar displayName en Firebase
                        auth.currentUser?.updateProfile(
                            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName("${usuario.nombre} ${usuario.apellidos}")
                                .build()
                        )?.await()
                    }
                    onSuccess()
                } else {
                    onError("Error al actualizar el perfil")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Sin conexión")
            }
        }
    }

    // ── Cambiar contraseña ──
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return

        viewModelScope.launch {
            try {
                // Re-autenticar antes de cambiar la contraseña
                val credential = com.google.firebase.auth.EmailAuthProvider
                    .getCredential(email, currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()
                onSuccess()
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                        "Contraseña actual incorrecta"
                    e.message?.contains("weak-password") == true ->
                        "La nueva contraseña es demasiado débil"
                    else -> e.message ?: "Error al cambiar contraseña"
                }
                onError(msg)
            }
        }
    }

    // ── Cargar sesión ──
    private suspend fun cargarSesion() {
        try {
            val uid = auth.currentUser?.uid ?: return
            val response = api.getMe(uid)
            if (response.isSuccessful) {
                val usuario = response.body() ?: return
                SessionManager.usuarioId  = usuario.id
                SessionManager.nombre     = usuario.nombre
                SessionManager.apellidos  = usuario.apellidos
                SessionManager.email      = usuario.email
                SessionManager.telefono   = usuario.telefono ?: ""

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
        } catch (_: Exception) { }
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
        SessionManager.clear()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

package com.example.autoelite_android.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

object SessionManager {

    private lateinit var dataStore: DataStore<Preferences>
    private val scope = CoroutineScope(Dispatchers.IO)

    // Keys
    private val KEY_CLIENTE_ID = longPreferencesKey("cliente_id")
    private val KEY_USUARIO_ID = longPreferencesKey("usuario_id")
    private val KEY_NOMBRE     = stringPreferencesKey("nombre")
    private val KEY_EMAIL      = stringPreferencesKey("email")
    private val KEY_PUNTOS     = intPreferencesKey("puntos")

    // acceso síncrono para el resto de la app
    var clienteId: Long = -1L
        set(value) { field = value; persist(KEY_CLIENTE_ID, value) }

    var usuarioId: Long = -1L
        set(value) { field = value; persist(KEY_USUARIO_ID, value) }

    var nombre: String = ""
        set(value) { field = value; persist(KEY_NOMBRE, value) }

    var email: String = ""
        set(value) { field = value; persist(KEY_EMAIL, value) }

    var puntos: Int = 0
        set(value) { field = value; persist(KEY_PUNTOS, value) }

    /**
     * Llamar una vez desde MainActivity.onCreate() ANTES de usar cualquier propiedad.
     * Carga los valores guardados en DataStore al caché en memoria.
     */
    fun init(context: Context) {
        dataStore = context.dataStore
        runBlocking {
            val prefs = dataStore.data.first()
            clienteId = prefs[KEY_CLIENTE_ID] ?: -1L
            usuarioId = prefs[KEY_USUARIO_ID] ?: -1L
            nombre    = prefs[KEY_NOMBRE]     ?: ""
            email     = prefs[KEY_EMAIL]      ?: ""
            puntos    = prefs[KEY_PUNTOS]     ?: 0
        }
    }

    /**
     * Limpia toda la sesión (logout).
     */
    fun clear() {
        clienteId = -1L
        usuarioId = -1L
        nombre    = ""
        email     = ""
        puntos    = 0
        scope.launch {
            dataStore.edit { it.clear() }
        }
    }

    // Helpers
    private fun <T> persist(key: Preferences.Key<T>, value: T) {
        if (!::dataStore.isInitialized) return
        scope.launch {
            dataStore.edit { prefs -> prefs[key] = value }
        }
    }
}
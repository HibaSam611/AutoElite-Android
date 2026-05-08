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

    // Keys de sesión
    private val KEY_CLIENTE_ID = longPreferencesKey("cliente_id")
    private val KEY_USUARIO_ID = longPreferencesKey("usuario_id")
    private val KEY_NOMBRE     = stringPreferencesKey("nombre")
    private val KEY_APELLIDOS  = stringPreferencesKey("apellidos")
    private val KEY_EMAIL      = stringPreferencesKey("email")
    private val KEY_TELEFONO   = stringPreferencesKey("telefono")
    private val KEY_PUNTOS     = intPreferencesKey("puntos")

    // Keys de notificaciones
    private val KEY_NOTIF_CITAS        = booleanPreferencesKey("notif_citas")
    private val KEY_NOTIF_REPARACIONES = booleanPreferencesKey("notif_reparaciones")
    private val KEY_NOTIF_PROMOCIONES  = booleanPreferencesKey("notif_promociones")

    // Key de tema
    private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")

    // Key de idioma
    private val KEY_LANGUAGE = stringPreferencesKey("language")

    // Cached values
    var clienteId: Long = -1L
        set(value) { field = value; persist(KEY_CLIENTE_ID, value) }

    var usuarioId: Long = -1L
        set(value) { field = value; persist(KEY_USUARIO_ID, value) }

    var nombre: String = ""
        set(value) { field = value; persist(KEY_NOMBRE, value) }

    var apellidos: String = ""
        set(value) { field = value; persist(KEY_APELLIDOS, value) }

    var email: String = ""
        set(value) { field = value; persist(KEY_EMAIL, value) }

    var telefono: String = ""
        set(value) { field = value; persist(KEY_TELEFONO, value) }

    var puntos: Int = 0
        set(value) { field = value; persist(KEY_PUNTOS, value) }

    var notifCitas: Boolean = true
        set(value) { field = value; persist(KEY_NOTIF_CITAS, value) }

    var notifReparaciones: Boolean = true
        set(value) { field = value; persist(KEY_NOTIF_REPARACIONES, value) }

    var notifPromociones: Boolean = false
        set(value) { field = value; persist(KEY_NOTIF_PROMOCIONES, value) }

    var isDarkMode: Boolean = false
        set(value) { field = value; persist(KEY_DARK_MODE, value) }

    var language: String = "es"
        set(value) { field = value; persist(KEY_LANGUAGE, value) }

    fun init(context: Context) {
        dataStore = context.dataStore
        runBlocking {
            val prefs = dataStore.data.first()
            clienteId  = prefs[KEY_CLIENTE_ID]  ?: -1L
            usuarioId  = prefs[KEY_USUARIO_ID]  ?: -1L
            nombre     = prefs[KEY_NOMBRE]      ?: ""
            apellidos  = prefs[KEY_APELLIDOS]   ?: ""
            email      = prefs[KEY_EMAIL]       ?: ""
            telefono   = prefs[KEY_TELEFONO]    ?: ""
            puntos     = prefs[KEY_PUNTOS]      ?: 0
            notifCitas        = prefs[KEY_NOTIF_CITAS]        ?: true
            notifReparaciones = prefs[KEY_NOTIF_REPARACIONES] ?: true
            notifPromociones  = prefs[KEY_NOTIF_PROMOCIONES]  ?: false
            isDarkMode        = prefs[KEY_DARK_MODE]          ?: false
            language          = prefs[KEY_LANGUAGE]            ?: "es"
        }
    }

    fun clear() {
        clienteId  = -1L
        usuarioId  = -1L
        nombre     = ""
        apellidos  = ""
        email      = ""
        telefono   = ""
        puntos     = 0
        scope.launch {
            dataStore.edit { prefs ->
                prefs.remove(KEY_CLIENTE_ID)
                prefs.remove(KEY_USUARIO_ID)
                prefs.remove(KEY_NOMBRE)
                prefs.remove(KEY_APELLIDOS)
                prefs.remove(KEY_EMAIL)
                prefs.remove(KEY_TELEFONO)
                prefs.remove(KEY_PUNTOS)
                // No borramos idioma ni tema al cerrar sesión
            }
        }
    }

    private fun <T> persist(key: Preferences.Key<T>, value: T) {
        if (!::dataStore.isInitialized) return
        scope.launch {
            dataStore.edit { prefs -> prefs[key] = value }
        }
    }
}

package com.example.autoelite_android.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleManager {

    const val SPANISH = "es"
    const val ENGLISH = "en"
    const val ARABIC  = "ar"

    /**
     * Devuelve un Context con el Locale del idioma guardado en SessionManager.
     * Llamar desde attachBaseContext() en MainActivity.
     */
    fun applyLocale(baseContext: Context): Context {
        val lang = SessionManager.language.ifBlank { SPANISH }
        return updateResources(baseContext, lang)
    }

    /**
     * Cambia el idioma y devuelve true para indicar que hay que recrear la Activity.
     */
    fun setLocale(context: Context, languageCode: String) {
        SessionManager.language = languageCode
    }

    /**
     * Nombre legible del idioma actual.
     */
    fun currentLanguageLabel(): String {
        return when (SessionManager.language.ifBlank { SPANISH }) {
            ENGLISH -> "English"
            ARABIC  -> "العربية"
            else    -> "Español"
        }
    }

    /**
     * Lista de idiomas disponibles: (código, nombre).
     */
    fun availableLanguages(): List<Pair<String, String>> = listOf(
        SPANISH to "Español",
        ENGLISH to "English",
        ARABIC  to "العربية"
    )

    // ── Interno ──

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)   // RTL para árabe

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        }

        return context.createConfigurationContext(config)
    }
}

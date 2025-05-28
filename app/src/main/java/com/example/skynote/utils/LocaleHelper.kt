package com.example.skynote.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale) // For RTL support

        return context.createConfigurationContext(config)
    }

    fun updateLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale) // For RTL support
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
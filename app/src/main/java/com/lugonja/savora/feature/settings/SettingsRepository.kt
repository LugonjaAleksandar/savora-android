package com.lugonja.savora.feature.settings

import com.lugonja.savora.core.storage.KeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppTheme { System, Light, Dark }

enum class AppLanguage(val tag: String?) {
    System(null),
    English("en"),
    Serbian("sr"),
}

interface SettingsRepository {
    val theme: Flow<AppTheme>
    val language: Flow<AppLanguage>

    suspend fun setTheme(theme: AppTheme)

    suspend fun setLanguage(language: AppLanguage)
}

class LocalSettingsRepository(private val store: KeyValueStore) : SettingsRepository {

    override val theme: Flow<AppTheme> = store.observe(THEME_KEY).map { stored ->
        AppTheme.entries.firstOrNull { it.name == stored } ?: AppTheme.System
    }

    /** First launch is in English; an explicit "System" choice is remembered. */
    override val language: Flow<AppLanguage> = store.observe(LANGUAGE_KEY).map { stored ->
        AppLanguage.entries.firstOrNull { it.name == stored } ?: AppLanguage.English
    }

    override suspend fun setTheme(theme: AppTheme) = store.set(THEME_KEY, theme.name)

    override suspend fun setLanguage(language: AppLanguage) = store.set(LANGUAGE_KEY, language.name)

    private companion object {
        const val THEME_KEY = "settings_theme"
        const val LANGUAGE_KEY = "settings_language"
    }
}

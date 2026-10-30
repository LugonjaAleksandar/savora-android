package com.lugonja.savora.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Writes the choice; `SavoraApplication` observes the language and applies it app-wide. */
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val theme: StateFlow<AppTheme> = repository.theme.stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.System)

    val language: StateFlow<AppLanguage> = repository.language.stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.English)

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { repository.setTheme(theme) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }
}

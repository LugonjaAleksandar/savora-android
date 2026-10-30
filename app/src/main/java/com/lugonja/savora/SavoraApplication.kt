package com.lugonja.savora

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.lugonja.savora.app.AppContainer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SavoraApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applyLanguageChoice()
    }

    /**
     * The stored language is the single source of truth: Settings writes it,
     * and this applies it through AppCompat's per-app locales (system-backed
     * on Android 13+).
     */
    private fun applyLanguageChoice() {
        MainScope().launch {
            container.settingsRepository.language.distinctUntilChanged().collect { language ->
                val locales = language.tag?.let(LocaleListCompat::forLanguageTags) ?: LocaleListCompat.getEmptyLocaleList()
                if (AppCompatDelegate.getApplicationLocales() != locales) {
                    AppCompatDelegate.setApplicationLocales(locales)
                }
            }
        }
    }
}

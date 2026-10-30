package com.lugonja.savora

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lugonja.savora.app.SavoraApp
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.feature.settings.AppTheme
import com.lugonja.savora.feature.settings.SettingsViewModel

/** AppCompat (rather than ComponentActivity) for per-app language support below Android 13. */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val container = (application as SavoraApplication).container

        setContent {
            val settings: SettingsViewModel = viewModel { SettingsViewModel(container.settingsRepository) }
            val theme by settings.theme.collectAsStateWithLifecycle()
            val darkTheme = when (theme) {
                AppTheme.System -> isSystemInDarkTheme()
                AppTheme.Light -> false
                AppTheme.Dark -> true
            }

            DisposableEffect(darkTheme) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            SavoraTheme(darkTheme = darkTheme) {
                SavoraApp(container = container, settings = settings)
            }
        }
    }
}

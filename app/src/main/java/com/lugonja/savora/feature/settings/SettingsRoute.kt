package com.lugonja.savora.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.GlowBackground
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.SlidingSelector
import com.lugonja.savora.core.designsystem.Spacing

@Composable
fun SettingsRoute(viewModel: SettingsViewModel) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val colors = SavoraTheme.colors

    GlowBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.md, bottom = Spacing.tabBarClearance),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Text(stringResource(R.string.settings_title), style = SavoraTheme.display(38), color = colors.textPrimary)

            SettingsGroup(stringResource(R.string.settings_theme), Icons.Rounded.Palette) {
                OptionSelector(AppTheme.entries, theme, viewModel::setTheme) { stringResource(it.label) }
            }
            SettingsGroup(stringResource(R.string.settings_language), Icons.Rounded.Language) {
                OptionSelector(AppLanguage.entries, language, viewModel::setLanguage) { it.displayName() }
            }
            SettingsGroup(stringResource(R.string.settings_about), Icons.Rounded.Info) {
                Text(stringResource(R.string.settings_about_body), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun <T> OptionSelector(options: List<T>, selected: T, onSelect: (T) -> Unit, label: @Composable (T) -> String) {
    val colors = SavoraTheme.colors
    SlidingSelector(items = options, selected = selected, onSelect = onSelect, modifier = Modifier.fillMaxWidth().height(46.dp)) { option, selectedness ->
        Text(label(option), style = MaterialTheme.typography.labelLarge, color = lerp(colors.textSecondary, Color.White, selectedness), maxLines = 1)
    }
}

@Composable
private fun SettingsGroup(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    val colors = SavoraTheme.colors
    val shape = RoundedCornerShape(Radius.lg)
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.stroke, shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Icon(icon, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
        content()
    }
}

private val AppTheme.label: Int
    get() = when (this) {
        AppTheme.System -> R.string.settings_theme_system
        AppTheme.Light -> R.string.settings_theme_light
        AppTheme.Dark -> R.string.settings_theme_dark
    }

/** Language names stay in their own language, so they are findable whatever the current one is. */
@Composable
private fun AppLanguage.displayName(): String = when (this) {
    AppLanguage.System -> stringResource(R.string.settings_language_system)
    AppLanguage.English -> "English"
    AppLanguage.Serbian -> "Srpski"
}

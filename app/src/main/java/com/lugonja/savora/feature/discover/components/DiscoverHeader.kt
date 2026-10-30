package com.lugonja.savora.feature.discover.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.GradientButton
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import java.time.LocalTime

@Composable
fun DiscoverHeader(isFetchingRandomMeal: Boolean, onSurpriseMe: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SavoraTheme.colors
    val title = buildAnnotatedString {
        append(stringResource(R.string.meals_header_title_prefix))
        append(" ")
        withStyle(SpanStyle(brush = colors.brandGradient, fontStyle = FontStyle.Italic)) {
            append(stringResource(R.string.meals_header_title_highlight))
        }
        append(stringResource(R.string.meals_header_title_suffix))
    }

    Column(modifier.padding(top = Spacing.md, bottom = Spacing.sm)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(greeting()),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            GradientButton(
                text = stringResource(R.string.meals_surprise_me),
                icon = Icons.Rounded.Casino,
                onClick = onSurpriseMe,
                isLoading = isFetchingRandomMeal,
            )
        }
        Text(title, style = SavoraTheme.display(38), color = colors.textPrimary, modifier = Modifier.padding(top = Spacing.md))
    }
}

private fun greeting(): Int {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> R.string.greeting_morning
        hour < 18 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }
}

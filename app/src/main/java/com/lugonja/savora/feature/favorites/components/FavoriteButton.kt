package com.lugonja.savora.feature.favorites.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.pressable

/** Glass heart toggle shared by the grid card and the detail screen. */
@Composable
fun FavoriteButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current
    Box(
        modifier
            .size(40.dp)
            .background(Color.Black.copy(alpha = 0.32f), CircleShape)
            .pressable(scale = 0.88f) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggle()
            },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = { scaleIn(spring(dampingRatio = 0.45f)) togetherWith scaleOut() },
            label = "favorite",
        ) { favorite ->
            Icon(
                imageVector = if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = null,
                tint = if (favorite) SavoraTheme.colors.primary else Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

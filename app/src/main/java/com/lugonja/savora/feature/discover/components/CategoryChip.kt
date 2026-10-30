package com.lugonja.savora.feature.discover.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.lugonja.savora.core.designsystem.RemoteImage
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.pressable
import com.lugonja.savora.feature.meals.model.MealCategory

/**
 * One category in [CategoryChips]. Transparent while selected, because the
 * sliding pill behind it provides the fill; it only reports clicks.
 */
@Composable
fun CategoryChip(category: MealCategory, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SavoraTheme.colors
    val background by animateColorAsState(if (isSelected) Color.Transparent else colors.surface, label = "chipBackground")
    val border by animateColorAsState(if (isSelected) Color.Transparent else colors.stroke, label = "chipBorder")
    val content by animateColorAsState(if (isSelected) Color.White else colors.textPrimary, label = "chipContent")
    val iconBackground by animateColorAsState(if (isSelected) Color.White.copy(alpha = 0.25f) else colors.surfaceRaised, label = "chipIcon")

    Row(
        modifier
            .height(56.dp)
            .background(background, CircleShape)
            .border(1.dp, border, CircleShape)
            .pressable(onClick = onClick)
            .padding(start = 6.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RemoteImage(
            url = category.thumbnailUrl,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(44.dp)
                .background(iconBackground, CircleShape)
                .padding(5.dp),
        )
        Text(category.name, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

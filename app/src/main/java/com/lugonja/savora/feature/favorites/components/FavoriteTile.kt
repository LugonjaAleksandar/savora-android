package com.lugonja.savora.feature.favorites.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.RemoteImage
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.designsystem.pressable
import com.lugonja.savora.core.ui.mealImageSharedElement
import com.lugonja.savora.feature.meals.model.MealSummary

/** One saved recipe. It only reports clicks; the Saved screen navigates. */
@Composable
fun FavoriteTile(meal: MealSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SavoraTheme.colors
    val shape = RoundedCornerShape(Radius.lg)
    val imageShape = RoundedCornerShape(Radius.md)
    Row(
        modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .background(colors.surface, shape)
            .pressable(onClick = onClick)
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        RemoteImage(
            url = meal.previewUrl,
            modifier = Modifier
                .size(84.dp)
                .mealImageSharedElement(meal.id, imageShape)
                .clip(imageShape),
        )
        Text(
            meal.name,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.padding(end = Spacing.sm))
    }
}

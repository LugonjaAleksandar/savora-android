package com.lugonja.savora.feature.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.RemoteImage
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.feature.meals.model.Ingredient

/** One ingredient in the detail grid. */
@Composable
fun IngredientTile(ingredient: Ingredient, modifier: Modifier = Modifier) {
    val colors = SavoraTheme.colors
    val shape = RoundedCornerShape(Radius.md)
    Row(
        modifier
            .height(72.dp)
            .background(colors.surface, shape)
            .border(1.dp, colors.stroke, shape)
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        RemoteImage(
            url = ingredient.imageUrl,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(52.dp)
                .background(colors.surfaceRaised, RoundedCornerShape(Radius.sm))
                .padding(6.dp),
        )
        Column {
            Text(
                ingredient.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (ingredient.measure.isNotEmpty()) {
                Text(ingredient.measure, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

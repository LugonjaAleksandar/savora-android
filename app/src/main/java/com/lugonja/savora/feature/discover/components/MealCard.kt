package com.lugonja.savora.feature.discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.RemoteImage
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.designsystem.pressable
import com.lugonja.savora.core.ui.mealImageSharedElement
import com.lugonja.savora.feature.favorites.components.FavoriteButton
import com.lugonja.savora.feature.meals.model.MealSummary

/** Grid item. It only reports clicks; the screen decides where to navigate. */
@Composable
fun MealCard(
    meal: MealSummary,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Radius.lg)
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(MealCardDefaults.ASPECT_RATIO)
            .shadow(16.dp, shape, ambientColor = SavoraTheme.colors.shadow, spotColor = SavoraTheme.colors.shadow)
            .pressable(onClick = onClick),
    ) {
        RemoteImage(
            url = meal.previewUrl,
            modifier = Modifier
                .fillMaxSize()
                .mealImageSharedElement(meal.id, shape)
                .clip(shape),
        )
        Box(
            Modifier
                .fillMaxSize()
                .clip(shape)
                .background(Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f))),
        )
        Text(
            text = meal.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.md),
        )
        FavoriteButton(
            isFavorite = isFavorite,
            onToggle = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
        )
    }
}

object MealCardDefaults {
    const val ASPECT_RATIO = 0.74f
}

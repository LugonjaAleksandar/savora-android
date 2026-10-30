package com.lugonja.savora.feature.discover.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.feature.meals.model.MealCategory

/**
 * Horizontal category picker. The gradient pill is a single view that springs
 * between chips, so only the selector moves while the chips never shift.
 */
@Composable
fun CategoryChips(
    categories: List<MealCategory>,
    selected: MealCategory?,
    isDimmed: Boolean,
    onSelect: (MealCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SavoraTheme.colors
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val chipBounds = remember { mutableStateMapOf<String, IntRect>() }
    val target = selected?.let { chipBounds[it.id] }
    val pillSpring = spring<Dp>(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
    val pillStart by animateDpAsState(with(density) { (target?.left ?: 0).toDp() }, pillSpring, label = "pillStart")
    val pillWidth by animateDpAsState(with(density) { (target?.width ?: 0).toDp() }, pillSpring, label = "pillWidth")
    val alpha by animateFloatAsState(if (isDimmed) 0.55f else 1f, label = "chipsAlpha")

    LaunchedEffect(target) {
        val bounds = target ?: return@LaunchedEffect
        val viewport = scrollState.viewportSize
        if (bounds.left < scrollState.value || bounds.right > scrollState.value + viewport) {
            scrollState.animateScrollTo((bounds.center.x - viewport / 2).coerceIn(0, scrollState.maxValue))
        }
    }

    Box(
        modifier
            .graphicsLayer { this.alpha = alpha }
            .horizontalScroll(scrollState)
            // Room for the selected chip's shadow, so it is not clipped by the scroll container.
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
    ) {
        if (target != null && !isDimmed) {
            Box(
                Modifier
                    .offset(x = pillStart)
                    .width(pillWidth)
                    .height(56.dp)
                    .shadow(12.dp, CircleShape, ambientColor = colors.primary, spotColor = colors.primary)
                    .background(colors.brandGradient, CircleShape),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEach { category ->
                CategoryChip(
                    category = category,
                    isSelected = category == selected && !isDimmed,
                    onClick = { onSelect(category) },
                    modifier = Modifier.onPlaced { coordinates ->
                        val position = coordinates.positionInParent()
                        chipBounds[category.id] = IntRect(position.x.toInt(), 0, position.x.toInt() + coordinates.size.width, coordinates.size.height)
                    },
                )
            }
        }
    }
}

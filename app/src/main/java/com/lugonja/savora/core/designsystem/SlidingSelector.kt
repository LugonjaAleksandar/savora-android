package com.lugonja.savora.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.floor

/**
 * Segmented control with one gradient thumb that slides between items. The
 * thumb position animates as a fraction, and each segment receives its
 * `selectedness` (0..1), so labels morph in step with the slide instead of
 * cross-fading. With [expandSelected] the selected segment grows to twice the
 * width of the others, interpolated with the thumb.
 */
@Composable
fun <T> SlidingSelector(
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    expandSelected: Boolean = false,
    segment: @Composable (item: T, selectedness: Float) -> Unit,
) {
    val colors = SavoraTheme.colors
    val position by animateFloatAsState(
        targetValue = items.indexOf(selected).coerceAtLeast(0).toFloat(),
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "selectorPosition",
    )
    BoxWithConstraints(modifier) {
        // Triangular weights sum to 1 for any position, so the total width stays constant.
        val selectedness = items.indices.map { (1f - abs(it - position)).coerceIn(0f, 1f) }
        val extra = if (expandSelected) 1f else 0f
        val unit: Dp = maxWidth / (items.size + extra)
        val widths = selectedness.map { unit * (1 + it * extra) }
        val starts = widths.runningFold(0.dp) { acc, width -> acc + width }

        val from = floor(position).toInt().coerceIn(0, items.lastIndex)
        val to = (from + 1).coerceAtMost(items.lastIndex)
        val fraction = (position - from).coerceIn(0f, 1f)
        val thumbStart = starts[from] + (starts[to] - starts[from]) * fraction
        val thumbWidth = widths[from] + (widths[to] - widths[from]) * fraction

        Box(
            Modifier
                .offset(x = thumbStart)
                .width(thumbWidth)
                .fillMaxHeight()
                .shadow(10.dp, CircleShape, ambientColor = colors.primary, spotColor = colors.primary)
                .background(colors.brandGradient, CircleShape),
        )
        items.forEachIndexed { index, item ->
            Box(
                Modifier
                    .offset(x = starts[index])
                    .width(widths[index])
                    .fillMaxHeight()
                    .clipToBounds()
                    .pressable { onSelect(item) },
                contentAlignment = Alignment.Center,
            ) {
                segment(item, selectedness[index])
            }
        }
    }
}

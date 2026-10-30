package com.lugonja.savora.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

/** Soft warm glows behind every tab, matching the Flutter `GlowBackground`. */
@Composable
fun GlowBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = SavoraTheme.colors
    // Radial gradients instead of blur: the same soft look, no clipped edges, and it works below Android 12.
    Box(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(listOf(colors.glowWarm, Color.Transparent), center = Offset(size.width * 0.1f, size.height * 0.05f), radius = size.width * 0.9f),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.1f, size.height * 0.05f),
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(colors.glowCool, Color.Transparent), center = Offset(size.width * 0.95f, size.height * 0.6f), radius = size.width * 0.85f),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.95f, size.height * 0.6f),
                )
            },
    ) {
        content()
    }
}

/** Clickable that scales down slightly while pressed, used by every tappable card. */
fun Modifier.pressable(scale: Float = 0.96f, onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(if (pressed) scale else 1f, spring(dampingRatio = 0.7f, stiffness = 600f), label = "pressScale")
    graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

@Composable
fun GradientButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, isLoading: Boolean = false) {
    val colors = SavoraTheme.colors
    Row(
        modifier
            .shadow(12.dp, CircleShape, ambientColor = colors.primary, spotColor = colors.primary)
            .background(colors.brandGradient, CircleShape)
            .pressable(onClick = { if (!isLoading) onClick() })
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun RatingStars(rating: Int, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    val colors = SavoraTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (star in 1..5) {
            Icon(
                imageVector = if (star <= rating) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = null,
                tint = if (star <= rating) colors.star else colors.textTertiary,
                modifier = Modifier.size(size),
            )
        }
    }
}

@Composable
fun StateMessage(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = SavoraTheme.colors
    Column(modifier.fillMaxWidth().padding(Spacing.xl), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Box(Modifier.size(76.dp).background(colors.brandGradient, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            GradientButton(text = actionLabel, icon = Icons.Rounded.Refresh, onClick = onAction, modifier = Modifier.padding(top = Spacing.sm))
        }
    }
}

/** Coil image with a warm shimmering placeholder and a short crossfade. */
@Composable
fun RemoteImage(url: String?, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val colors = SavoraTheme.colors
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(250).build(),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
        loading = { Box(Modifier.fillMaxSize().background(colors.surfaceRaised).shimmer()) },
        error = {
            Box(Modifier.fillMaxSize().background(colors.surfaceRaised), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = colors.textTertiary)
            }
        },
    )
}

/** Sweeping highlight used for skeletons and image placeholders. */
fun Modifier.shimmer(): Modifier = composed {
    val progress by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    drawWithContent {
        drawContent()
        val x = size.width * progress
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
                start = Offset(x - size.width * 0.3f, 0f),
                end = Offset(x + size.width * 0.3f, 0f),
            ),
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(text, style = SavoraTheme.display(24), color = SavoraTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = SavoraTheme.colors.textTertiary)
        }
    }
}

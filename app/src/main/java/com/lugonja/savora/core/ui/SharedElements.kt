package com.lugonja.savora.core.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Marks a meal photo as the shared element between the grid or Saved list and
 * the detail header, so the image flies between screens. A no-op in previews
 * and anywhere outside the navigation graph.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.mealImageSharedElement(mealId: String, shape: Shape): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@mealImageSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "meal-image-$mealId"),
            animatedVisibilityScope = visibilityScope,
            clipInOverlayDuringTransition = OverlayClip(shape),
        )
    }
}

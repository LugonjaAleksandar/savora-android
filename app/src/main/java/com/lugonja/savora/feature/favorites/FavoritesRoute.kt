package com.lugonja.savora.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.GlowBackground
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.designsystem.StateMessage
import com.lugonja.savora.core.ui.quantityString
import com.lugonja.savora.feature.favorites.components.FavoriteTile
import com.lugonja.savora.feature.meals.model.MealSummary

@Composable
fun FavoritesRoute(
    viewModel: FavoritesViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenMeal: (MealSummary) -> Unit,
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = SavoraTheme.colors

    LaunchedEffect(viewModel) {
        viewModel.removals.collect { removal ->
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.favorites_removed),
                actionLabel = context.getString(R.string.favorites_undo),
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undo(removal)
        }
    }

    GlowBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, top = Spacing.md, bottom = Spacing.tabBarClearance),
        ) {
            item(key = "header") {
                Column(Modifier.padding(bottom = Spacing.lg)) {
                    Text(stringResource(R.string.favorites_title), style = SavoraTheme.display(38), color = colors.textPrimary)
                    Text(
                        quantityString(R.plurals.favorites_subtitle, favorites.size, zero = R.string.favorites_subtitle_zero),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
            }
            if (favorites.isEmpty()) {
                item(key = "empty") {
                    StateMessage(
                        icon = Icons.Rounded.Favorite,
                        title = stringResource(R.string.favorites_empty_title),
                        subtitle = stringResource(R.string.favorites_empty_subtitle),
                        modifier = Modifier.padding(top = 96.dp),
                    )
                }
            }
            items(favorites, key = { it.id }) { meal ->
                SwipeToRemove(onRemove = { viewModel.remove(meal) }, modifier = Modifier.animateItem().padding(bottom = Spacing.md)) {
                    FavoriteTile(meal = meal, onClick = { onOpenMeal(meal) })
                }
            }
        }
    }
}

@Composable
private fun SwipeToRemove(onRemove: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    var isRemoved by remember { mutableStateOf(false) }
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            // The swipe state is saveable and keyed by recipe, so the list would restore
            // "dismissed" when the same recipe comes back (undo, or the heart on Discover)
            // and remove it again. Settle it right after removing, and hide the row while
            // it animates out. Settling restarts this effect, so it must come last.
            isRemoved = true
            onRemove()
            state.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        modifier = modifier.graphicsLayer { alpha = if (isRemoved) 0f else 1f },
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(SavoraTheme.colors.danger, RoundedCornerShape(Radius.lg))
                    .padding(end = Spacing.lg),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Color.White)
            }
        },
    ) { content() }
}

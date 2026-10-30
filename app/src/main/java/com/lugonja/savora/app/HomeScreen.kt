package com.lugonja.savora.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.SlidingSelector
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.ui.LocalNavAnimatedVisibilityScope
import com.lugonja.savora.feature.discover.DiscoverRoute
import com.lugonja.savora.feature.discover.DiscoverViewModel
import com.lugonja.savora.feature.favorites.FavoritesRoute
import com.lugonja.savora.feature.favorites.FavoritesViewModel
import com.lugonja.savora.feature.meals.model.MealSummary
import com.lugonja.savora.feature.settings.SettingsRoute
import com.lugonja.savora.feature.settings.SettingsViewModel

enum class HomeTab { Discover, Favorites, Settings }

/** The three tabs stay composed, so each keeps its scroll position and state while hidden. */
@Composable
fun HomeScreen(
    container: AppContainer,
    favorites: FavoritesViewModel,
    settings: SettingsViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenMeal: (MealSummary) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Discover) }
    val discover: DiscoverViewModel = viewModel { DiscoverViewModel(container.mealsRepository) }

    Box(Modifier.fillMaxSize()) {
        HomeTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val alpha by animateFloatAsState(if (isSelected) 1f else 0f, tween(220), label = "tabAlpha")
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(if (isSelected) 1f else 0f)
                    .graphicsLayer { this.alpha = alpha }
                    .then(if (isSelected) Modifier else Modifier.clearAndSetSemantics {}),
            ) {
                // Hidden tabs stay composed; only the visible one may own the shared photo keys.
                CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides LocalNavAnimatedVisibilityScope.current.takeIf { isSelected }) {
                    when (tab) {
                        HomeTab.Discover -> DiscoverRoute(discover, favorites, snackbarHostState, onOpenMeal)
                        HomeTab.Favorites -> FavoritesRoute(favorites, snackbarHostState, onOpenMeal)
                        HomeTab.Settings -> SettingsRoute(settings)
                    }
                }
            }
        }

        FloatingTabBar(
            selected = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier
                .zIndex(2f)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        )
    }
}

@Composable
private fun FloatingTabBar(selected: HomeTab, onSelect: (HomeTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = SavoraTheme.colors
    SlidingSelector(
        items = HomeTab.entries,
        selected = selected,
        onSelect = onSelect,
        expandSelected = true,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(24.dp, CircleShape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .background(colors.glass, CircleShape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)), CircleShape)
            .padding(6.dp),
    ) { tab, selectedness ->
        val tint = lerp(colors.textSecondary, Color.White, selectedness)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Icon(tab.icon(selectedness > 0.5f), contentDescription = stringResource(tab.label), tint = tint, modifier = Modifier.size(22.dp))
            if (selectedness > 0.5f) {
                Text(
                    text = stringResource(tab.label),
                    color = tint,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.graphicsLayer { alpha = (selectedness - 0.5f) * 2f },
                )
            }
        }
    }
}

private val HomeTab.label: Int
    get() = when (this) {
        HomeTab.Discover -> R.string.nav_discover
        HomeTab.Favorites -> R.string.nav_favorites
        HomeTab.Settings -> R.string.nav_settings
    }

private fun HomeTab.icon(isSelected: Boolean) = when (this) {
    HomeTab.Discover -> if (isSelected) Icons.Rounded.Explore else Icons.Outlined.Explore
    HomeTab.Favorites -> if (isSelected) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder
    HomeTab.Settings -> Icons.Rounded.Tune
}

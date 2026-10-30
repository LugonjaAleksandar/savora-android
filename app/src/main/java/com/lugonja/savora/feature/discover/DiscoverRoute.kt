package com.lugonja.savora.feature.discover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RamenDining
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.GlowBackground
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.designsystem.StateMessage
import com.lugonja.savora.core.designsystem.shimmer
import com.lugonja.savora.core.ui.bleedHorizontally
import com.lugonja.savora.core.ui.message
import com.lugonja.savora.core.ui.messageRes
import com.lugonja.savora.core.ui.quantityString
import com.lugonja.savora.feature.discover.components.CategoryChips
import com.lugonja.savora.feature.discover.components.DiscoverHeader
import com.lugonja.savora.feature.discover.components.MealCard
import com.lugonja.savora.feature.discover.components.MealCardDefaults
import com.lugonja.savora.feature.discover.components.SearchField
import com.lugonja.savora.feature.favorites.FavoritesViewModel
import com.lugonja.savora.feature.meals.model.MealSummary
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun DiscoverRoute(
    viewModel: DiscoverViewModel,
    favorites: FavoritesViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenMeal: (MealSummary) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteMeals by favorites.favorites.collectAsStateWithLifecycle()
    val favoriteIds = remember(favoriteMeals) { favoriteMeals.mapTo(HashSet()) { it.id } }
    var searchText by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DiscoverEvent.OpenMeal -> onOpenMeal(event.meal)
                is DiscoverEvent.ShowFailure -> snackbarHostState.showSnackbar(context.getString(event.failure.messageRes))
            }
        }
    }

    GlowBackground {
        CollapsingHeaderLayout(
            header = {
                DiscoverHeader(
                    isFetchingRandomMeal = state.isFetchingRandomMeal,
                    onSurpriseMe = viewModel::surpriseMe,
                    modifier = Modifier.padding(horizontal = Spacing.lg),
                )
            },
            pinned = {
                SearchField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.onSearchChange(it)
                    },
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                )
            },
        ) {
            PullToRefreshBoxCompat(isRefreshing = state.isRefreshing, onRefresh = viewModel::refresh) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = rememberLazyGridState(),
                    contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.tabBarClearance),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    fullWidthItem("chips") {
                        CategoryChips(
                            categories = state.categories,
                            selected = state.selectedCategory,
                            isDimmed = state.isSearching,
                            onSelect = { category ->
                                searchText = ""
                                focusManager.clearFocus()
                                viewModel.selectCategory(category)
                            },
                            modifier = Modifier.bleedHorizontally(Spacing.lg),
                        )
                    }
                    fullWidthItem("title") { SectionHeader(state) }
                    content(state, favoriteIds, favorites::toggle, onOpenMeal, viewModel::refresh)
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidthItem(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun LazyGridScope.content(
    state: DiscoverUiState,
    favoriteIds: Set<String>,
    onToggleFavorite: (MealSummary) -> Unit,
    onOpenMeal: (MealSummary) -> Unit,
    onRetry: () -> Unit,
) {
    when (val status = state.status) {
        DiscoverUiState.Status.Loading -> items(6) {
            Box(
                Modifier
                    .aspectRatio(MealCardDefaults.ASPECT_RATIO)
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(SavoraTheme.colors.surfaceRaised)
                    .shimmer(),
            )
        }
        is DiscoverUiState.Status.Failed -> fullWidthItem("failure") {
            StateMessage(icon = Icons.Rounded.WifiOff, title = status.failure.message(), actionLabel = stringResource(R.string.action_retry), onAction = onRetry)
        }
        DiscoverUiState.Status.Loaded -> if (state.meals.isEmpty()) {
            fullWidthItem("empty") {
                StateMessage(
                    icon = Icons.Rounded.RamenDining,
                    title = stringResource(R.string.meals_empty_title),
                    subtitle = stringResource(R.string.meals_empty_subtitle),
                )
            }
        } else {
            val resultKey = "${state.selectedCategory?.id}-${state.query}"
            itemsIndexed(state.meals, key = { _, meal -> "$resultKey-${meal.id}" }) { index, meal ->
                StaggeredEntrance(index) {
                    MealCard(
                        meal = meal,
                        isFavorite = meal.id in favoriteIds,
                        onClick = { onOpenMeal(meal) },
                        onToggleFavorite = { onToggleFavorite(meal) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(state: DiscoverUiState) {
    val colors = SavoraTheme.colors
    val title = when {
        state.isSearching -> stringResource(R.string.meals_section_search, state.query)
        state.selectedCategory != null -> stringResource(R.string.meals_section_category, state.selectedCategory.name)
        else -> ""
    }
    Row(Modifier.fillMaxWidth().padding(bottom = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = SavoraTheme.display(24), color = colors.textPrimary, maxLines = 1, modifier = Modifier.weight(1f))
        if (state.status == DiscoverUiState.Status.Loaded) {
            Text(
                text = quantityString(R.plurals.meals_count, state.meals.size, zero = R.string.meals_count_zero),
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.1f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/** Cards fade, rise and scale in one after another whenever a new result set arrives. */
@Composable
private fun StaggeredEntrance(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50L * (index % 8))
        visible = true
    }
    val progress by animateFloatAsState(if (visible) 1f else 0f, spring(dampingRatio = 0.85f, stiffness = 300f), label = "entrance")
    Box(
        Modifier.graphicsLayer {
            alpha = progress
            translationY = (1 - progress) * 24.dp.toPx()
            scaleX = 0.96f + 0.04f * progress
            scaleY = 0.96f + 0.04f * progress
        },
    ) { content() }
}

/**
 * Greeting and title collapse with the first scroll while the search field
 * stays pinned under the status bar, and come back when scrolling up.
 */
@Composable
private fun CollapsingHeaderLayout(header: @Composable () -> Unit, pinned: @Composable () -> Unit, content: @Composable () -> Unit) {
    var headerHeight by remember { mutableIntStateOf(0) }
    var offset by remember { mutableFloatStateOf(0f) }
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = consume(available.y.takeIf { it < 0 })

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = consume(available.y.takeIf { it > 0 })

            private fun consume(delta: Float?): Offset {
                if (delta == null) return Offset.Zero
                val updated = (offset + delta).coerceIn(-headerHeight.toFloat(), 0f)
                val consumed = updated - offset
                offset = updated
                return Offset(0f, consumed)
            }
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().nestedScroll(connection)) {
        Box(
            Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
                    headerHeight = placeable.height
                    val visibleHeight = (placeable.height + offset).roundToInt().coerceAtLeast(0)
                    layout(placeable.width, visibleHeight) { placeable.place(0, offset.roundToInt()) }
                }
                .graphicsLayer { alpha = if (headerHeight == 0) 1f else 1f + offset / headerHeight },
        ) { header() }
        pinned()
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PullToRefreshBoxCompat(isRefreshing: Boolean, onRefresh: () -> Unit, content: @Composable () -> Unit) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) { content() }
}

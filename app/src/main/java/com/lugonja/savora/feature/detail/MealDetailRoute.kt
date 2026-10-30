package com.lugonja.savora.feature.detail

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NoMeals
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.RemoteImage
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.designsystem.StateMessage
import com.lugonja.savora.core.designsystem.pressable
import com.lugonja.savora.core.designsystem.shimmer
import com.lugonja.savora.core.ui.mealImageSharedElement
import com.lugonja.savora.core.ui.message
import com.lugonja.savora.feature.detail.components.MealDetailContent
import com.lugonja.savora.feature.favorites.FavoritesViewModel
import com.lugonja.savora.feature.favorites.components.FavoriteButton
import com.lugonja.savora.feature.meals.model.MealSummary
import com.lugonja.savora.feature.reviews.ReviewSheet
import kotlinx.coroutines.launch

private val IMAGE_HEIGHT = 380.dp
private val SHEET_OVERLAP = 32.dp

/** Owns the review sheet, the confirmation snackbars and opening links; the content only reports intents. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailRoute(
    viewModel: MealDetailViewModel,
    favorites: FavoritesViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val review by viewModel.review.collectAsStateWithLifecycle()
    val favoriteMeals by favorites.favorites.collectAsStateWithLifecycle()
    val isFavorite = favoriteMeals.any { it.id == viewModel.preview.id }
    var isReviewSheetOpen by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val summary = (state as? MealDetailUiState.Loaded)?.meal?.summary ?: viewModel.preview

    LightStatusBarIcons()

    Box(Modifier.fillMaxSize().background(SavoraTheme.colors.background)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item(key = "header") {
                HeaderImage(
                    meal = summary,
                    modifier = Modifier.graphicsLayer {
                        // Parallax: the photo scrolls at half speed under the sheet.
                        if (listState.firstVisibleItemIndex == 0) translationY = listState.firstVisibleItemScrollOffset * 0.5f
                    },
                )
            }
            item(key = "content") {
                Box(
                    Modifier
                        .offset(y = -SHEET_OVERLAP)
                        .fillMaxWidth()
                        .background(SavoraTheme.colors.background, RoundedCornerShape(topStart = SHEET_OVERLAP, topEnd = SHEET_OVERLAP))
                        .padding(horizontal = Spacing.lg)
                        .padding(top = Spacing.lg)
                        .navigationBarsPadding(),
                ) {
                    AnimatedContent(
                        targetState = state,
                        transitionSpec = { (fadeIn(tween(350)) + slideInVertically(tween(450)) { it / 12 }) togetherWith fadeOut(tween(200)) },
                        contentKey = { it::class },
                        label = "detailContent",
                    ) { current ->
                        when (current) {
                            MealDetailUiState.Loading -> DetailSkeleton(title = viewModel.preview.name)
                            is MealDetailUiState.Failed -> StateMessage(
                                icon = Icons.Rounded.NoMeals,
                                title = current.failure.message(),
                                actionLabel = stringResource(R.string.action_retry),
                                onAction = viewModel::load,
                            )
                            is MealDetailUiState.Loaded -> MealDetailContent(
                                meal = current.meal,
                                review = review,
                                onWriteReview = { isReviewSheetOpen = true },
                                onDeleteReview = {
                                    viewModel.deleteReview()
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.review_deleted)) }
                                },
                                onOpenLink = uriHandler::openUri,
                            )
                        }
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.32f), CircleShape)
                    .pressable(scale = 0.88f, onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
            }
            FavoriteButton(isFavorite = isFavorite, onToggle = { favorites.toggle(summary) })
        }
    }

    if (isReviewSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isReviewSheetOpen = false },
            sheetState = sheetState,
            containerColor = SavoraTheme.colors.background,
        ) {
            ReviewSheet(initialReview = review) { rating, comment ->
                viewModel.saveReview(rating, comment)
                scope.launch {
                    sheetState.hide()
                    isReviewSheetOpen = false
                    snackbarHostState.showSnackbar(context.getString(R.string.review_saved))
                }
            }
        }
    }
}

/**
 * The preview the grid already decoded sits underneath, so the shared-element
 * flight never lands on an empty frame; the full-resolution photo loads over it.
 */
@Composable
private fun HeaderImage(meal: MealSummary, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(IMAGE_HEIGHT)
            .mealImageSharedElement(meal.id, RoundedCornerShape(0.dp)),
    ) {
        RemoteImage(url = meal.previewUrl, modifier = Modifier.fillMaxSize())
        RemoteImage(url = meal.thumbnailUrl, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.35f), 0.4f to Color.Transparent)))
    }
}

@Composable
private fun DetailSkeleton(title: String) {
    val colors = SavoraTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(title, style = SavoraTheme.display(30), color = colors.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            repeat(3) { Box(Modifier.size(90.dp, 34.dp).clip(CircleShape).background(colors.surfaceRaised).shimmer()) }
        }
        Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(Radius.lg)).background(colors.surfaceRaised).shimmer())
        repeat(4) {
            Box(Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(Radius.sm)).background(colors.surfaceRaised).shimmer())
        }
    }
}

/** White status-bar icons over the photo, restored when leaving the screen. */
@Composable
private fun LightStatusBarIcons() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}

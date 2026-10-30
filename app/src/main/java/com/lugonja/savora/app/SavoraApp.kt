package com.lugonja.savora.app

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.ui.LocalNavAnimatedVisibilityScope
import com.lugonja.savora.core.ui.LocalSharedTransitionScope
import com.lugonja.savora.feature.detail.MealDetailRoute
import com.lugonja.savora.feature.detail.MealDetailViewModel
import com.lugonja.savora.feature.favorites.FavoritesViewModel
import com.lugonja.savora.feature.meals.model.MealSummary
import com.lugonja.savora.feature.settings.SettingsViewModel
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class MealRoute(val id: String, val name: String, val thumbnailUrl: String) {
    val summary: MealSummary get() = MealSummary(id, name, thumbnailUrl)
}

/**
 * Navigation graph. Route-level screens decide *when* to navigate and call the
 * callbacks below; cards and tiles only expose `onClick`.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SavoraApp(container: AppContainer, settings: SettingsViewModel) {
    val navController = rememberNavController()
    val favorites: FavoritesViewModel = viewModel { FavoritesViewModel(container.favoritesRepository) }
    val snackbarHostState = remember { SnackbarHostState() }
    val openMeal: (MealSummary) -> Unit = { meal ->
        navController.navigate(MealRoute(meal.id, meal.name, meal.thumbnailUrl)) { launchSingleTop = true }
    }

    Box(Modifier.fillMaxSize()) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    enterTransition = { fadeIn(tween(450)) },
                    exitTransition = { fadeOut(tween(450)) },
                    popEnterTransition = { fadeIn(tween(400)) },
                    popExitTransition = { fadeOut(tween(400)) },
                ) {
                    composable<HomeRoute> {
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            HomeScreen(
                                container = container,
                                favorites = favorites,
                                settings = settings,
                                snackbarHostState = snackbarHostState,
                                onOpenMeal = openMeal,
                            )
                        }
                    }
                    composable<MealRoute> { entry ->
                        val route = entry.toRoute<MealRoute>()
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            MealDetailRoute(
                                viewModel = viewModel { MealDetailViewModel(route.summary, container.mealsRepository, container.reviewsRepository) },
                                favorites = favorites,
                                snackbarHostState = snackbarHostState,
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 88.dp),
        ) { data ->
            SavoraSnackbar(message = data.visuals.message, actionLabel = data.visuals.actionLabel, onAction = data::performAction)
        }
    }
}

@Composable
private fun SavoraSnackbar(message: String, actionLabel: String?, onAction: () -> Unit) {
    val colors = SavoraTheme.colors
    val shape = RoundedCornerShape(Radius.md)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md)
            .shadow(16.dp, shape)
            .background(colors.textPrimary, shape)
            .padding(start = Spacing.md + 4.dp, end = Spacing.sm, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            color = colors.background,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
        )
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = colors.star, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

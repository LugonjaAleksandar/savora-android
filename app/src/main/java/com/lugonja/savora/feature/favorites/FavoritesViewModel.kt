package com.lugonja.savora.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lugonja.savora.feature.meals.model.MealSummary
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Emitted after a swipe-to-remove, so the screen can offer an undo. */
data class FavoriteRemoved(val meal: MealSummary, val index: Int)

/**
 * Activity-scoped, because the heart appears on grid cards, the detail screen
 * and the Saved tab at the same time and they must agree.
 */
class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {

    val favorites: StateFlow<List<MealSummary>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = emptyList())

    private val _removals = Channel<FavoriteRemoved>(Channel.BUFFERED)
    val removals: Flow<FavoriteRemoved> = _removals.receiveAsFlow()

    fun toggle(meal: MealSummary) {
        viewModelScope.launch { repository.toggle(meal) }
    }

    fun remove(meal: MealSummary) {
        viewModelScope.launch {
            repository.remove(meal.id)?.let { _removals.send(FavoriteRemoved(meal, it)) }
        }
    }

    fun undo(removal: FavoriteRemoved) {
        viewModelScope.launch { repository.add(removal.meal, removal.index) }
    }
}

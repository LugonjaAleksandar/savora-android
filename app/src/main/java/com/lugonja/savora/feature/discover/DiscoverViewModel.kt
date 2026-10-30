package com.lugonja.savora.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lugonja.savora.core.network.AppFailure
import com.lugonja.savora.core.network.Outcome
import com.lugonja.savora.feature.meals.data.MealsRepository
import com.lugonja.savora.feature.meals.model.MealCategory
import com.lugonja.savora.feature.meals.model.MealSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class DiscoverUiState(
    val status: Status = Status.Loading,
    val categories: List<MealCategory> = emptyList(),
    val selectedCategory: MealCategory? = null,
    val meals: List<MealSummary> = emptyList(),
    val query: String = "",
    val isFetchingRandomMeal: Boolean = false,
    val isRefreshing: Boolean = false,
) {
    val isSearching: Boolean get() = query.isNotEmpty()

    sealed interface Status {
        data object Loading : Status
        data object Loaded : Status
        data class Failed(val failure: AppFailure) : Status
    }
}

/** One-shot effects. Navigating is the screen's job, so the random pick is sent, not stored. */
sealed interface DiscoverEvent {
    data class OpenMeal(val meal: MealSummary) : DiscoverEvent
    data class ShowFailure(val failure: AppFailure) : DiscoverEvent
}

/**
 * Every load is *restartable*: starting one cancels the previous job, so a
 * slow response can never overwrite a newer selection. Search is debounced
 * the same way.
 */
class DiscoverViewModel(
    private val repository: MealsRepository,
    private val searchDebounce: Duration = SEARCH_DEBOUNCE,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _events = Channel<DiscoverEvent>(Channel.BUFFERED)
    val events: Flow<DiscoverEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        restart { loadCategoriesAndMeals(forceRefresh = false) }
    }

    fun selectCategory(category: MealCategory) {
        val state = _uiState.value
        if (category == state.selectedCategory && !state.isSearching && state.status == DiscoverUiState.Status.Loaded) return
        _uiState.update { it.copy(selectedCategory = category, query = "", status = DiscoverUiState.Status.Loading) }
        restart { loadMeals(category) }
    }

    fun onSearchChange(text: String) {
        val query = text.trim()
        if (query == _uiState.value.query) return
        _uiState.update { it.copy(query = query, status = DiscoverUiState.Status.Loading) }
        restart {
            if (query.isEmpty()) {
                _uiState.value.selectedCategory?.let { loadMeals(it) }
            } else {
                delay(searchDebounce)
                apply(repository.search(query))
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        restart {
            val state = _uiState.value
            when {
                state.isSearching -> apply(repository.search(state.query))
                state.selectedCategory != null -> loadMeals(state.selectedCategory)
                else -> loadCategoriesAndMeals(forceRefresh = true)
            }
        }.invokeOnCompletion { _uiState.update { it.copy(isRefreshing = false) } }
    }

    fun surpriseMe() {
        if (_uiState.value.isFetchingRandomMeal) return
        _uiState.update { it.copy(isFetchingRandomMeal = true) }
        viewModelScope.launch {
            val event = when (val outcome = repository.randomMeal()) {
                is Outcome.Success -> DiscoverEvent.OpenMeal(outcome.data.summary)
                is Outcome.Failure -> DiscoverEvent.ShowFailure(outcome.failure)
            }
            _uiState.update { it.copy(isFetchingRandomMeal = false) }
            _events.send(event)
        }
    }

    private fun restart(block: suspend () -> Unit): Job {
        loadJob?.cancel()
        return viewModelScope.launch { block() }.also { loadJob = it }
    }

    private suspend fun loadCategoriesAndMeals(forceRefresh: Boolean) {
        when (val outcome = repository.categories(forceRefresh)) {
            is Outcome.Success -> {
                val selected = _uiState.value.selectedCategory ?: outcome.data.firstOrNull()
                _uiState.update { it.copy(categories = outcome.data, selectedCategory = selected) }
                if (selected != null) {
                    loadMeals(selected)
                } else {
                    _uiState.update { it.copy(meals = emptyList(), status = DiscoverUiState.Status.Loaded) }
                }
            }
            is Outcome.Failure -> _uiState.update { it.copy(status = DiscoverUiState.Status.Failed(outcome.failure)) }
        }
    }

    private suspend fun loadMeals(category: MealCategory) = apply(repository.mealsInCategory(category.name))

    private fun apply(outcome: Outcome<List<MealSummary>>) {
        _uiState.update {
            when (outcome) {
                is Outcome.Success -> it.copy(meals = outcome.data, status = DiscoverUiState.Status.Loaded)
                is Outcome.Failure -> it.copy(status = DiscoverUiState.Status.Failed(outcome.failure))
            }
        }
    }

    companion object {
        val SEARCH_DEBOUNCE = 350.milliseconds
    }
}

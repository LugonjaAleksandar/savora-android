package com.lugonja.savora.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lugonja.savora.core.network.AppFailure
import com.lugonja.savora.core.network.Outcome
import com.lugonja.savora.feature.meals.data.MealsRepository
import com.lugonja.savora.feature.meals.model.MealDetail
import com.lugonja.savora.feature.meals.model.MealSummary
import com.lugonja.savora.feature.reviews.MealReview
import com.lugonja.savora.feature.reviews.ReviewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MealDetailUiState {
    data object Loading : MealDetailUiState
    data class Loaded(val meal: MealDetail) : MealDetailUiState
    data class Failed(val failure: AppFailure) : MealDetailUiState
}

class MealDetailViewModel(
    val preview: MealSummary,
    private val mealsRepository: MealsRepository,
    private val reviewsRepository: ReviewsRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MealDetailUiState>(MealDetailUiState.Loading)
    val uiState: StateFlow<MealDetailUiState> = _uiState.asStateFlow()

    val review: StateFlow<MealReview?> = reviewsRepository.review(preview.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    init {
        load()
    }

    fun load() {
        _uiState.value = MealDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val outcome = mealsRepository.meal(preview.id)) {
                is Outcome.Success -> MealDetailUiState.Loaded(outcome.data)
                is Outcome.Failure -> MealDetailUiState.Failed(outcome.failure)
            }
        }
    }

    fun saveReview(rating: Int, comment: String) {
        viewModelScope.launch {
            reviewsRepository.save(MealReview(mealId = preview.id, rating = rating, comment = comment.trim(), updatedAtMillis = now()))
        }
    }

    fun deleteReview() {
        viewModelScope.launch { reviewsRepository.delete(preview.id) }
    }
}

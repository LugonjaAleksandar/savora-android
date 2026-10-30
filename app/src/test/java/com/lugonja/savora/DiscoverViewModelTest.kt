package com.lugonja.savora

import app.cash.turbine.test
import com.lugonja.savora.core.network.AppFailure
import com.lugonja.savora.core.network.Outcome
import com.lugonja.savora.feature.discover.DiscoverEvent
import com.lugonja.savora.feature.discover.DiscoverUiState
import com.lugonja.savora.feature.discover.DiscoverViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = FakeMealsRepository()

    @Test
    fun `starts by loading categories and the first category`() = runTest(mainRule.dispatcher) {
        val viewModel = DiscoverViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DiscoverUiState.Status.Loaded, state.status)
        assertEquals("Beef", state.selectedCategory?.name)
        assertEquals(listOf(Fixtures.summary), state.meals)
        assertEquals(listOf("Beef"), repository.requestedCategories)
    }

    @Test
    fun `selecting a category loads its meals`() = runTest(mainRule.dispatcher) {
        val viewModel = DiscoverViewModel(repository)
        advanceUntilIdle()

        viewModel.selectCategory(Fixtures.categories[1])
        advanceUntilIdle()

        assertEquals("Chicken", viewModel.uiState.value.selectedCategory?.name)
        assertEquals(listOf("Beef", "Chicken"), repository.requestedCategories)
    }

    @Test
    fun `search is debounced so only the last query hits the repository`() = runTest(mainRule.dispatcher) {
        val viewModel = DiscoverViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchChange("c")
        advanceTimeBy(100)
        viewModel.onSearchChange("ch")
        advanceTimeBy(100)
        viewModel.onSearchChange("chicken")
        advanceUntilIdle()

        assertEquals(listOf("chicken"), repository.searchedQueries)
        assertTrue(viewModel.uiState.value.isSearching)
    }

    @Test
    fun `clearing the search returns to the selected category`() = runTest(mainRule.dispatcher) {
        val viewModel = DiscoverViewModel(repository)
        advanceUntilIdle()
        viewModel.onSearchChange("chicken")
        advanceUntilIdle()

        viewModel.onSearchChange("")
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isSearching)
        assertEquals(listOf("Beef", "Beef"), repository.requestedCategories)
    }

    @Test
    fun `failure to load categories is surfaced`() = runTest(mainRule.dispatcher) {
        repository.categoriesOutcome = Outcome.Failure(AppFailure.Network)

        val viewModel = DiscoverViewModel(repository)
        advanceUntilIdle()

        assertEquals(DiscoverUiState.Status.Failed(AppFailure.Network), viewModel.uiState.value.status)
    }

    @Test
    fun `surprise me emits a navigation event instead of navigating`() = runTest(mainRule.dispatcher) {
        val viewModel = DiscoverViewModel(repository)

        viewModel.events.test {
            viewModel.surpriseMe()
            assertEquals(DiscoverEvent.OpenMeal(Fixtures.detail.summary), awaitItem())
        }
    }
}

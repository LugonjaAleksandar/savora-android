package com.lugonja.savora

import app.cash.turbine.test
import com.lugonja.savora.core.storage.InMemoryKeyValueStore
import com.lugonja.savora.feature.favorites.FavoriteRemoved
import com.lugonja.savora.feature.favorites.FavoritesViewModel
import com.lugonja.savora.feature.favorites.LocalFavoritesRepository
import com.lugonja.savora.feature.meals.data.toMealDetail
import com.lugonja.savora.feature.meals.model.Ingredient
import com.lugonja.savora.feature.meals.model.MealSummary
import com.lugonja.savora.feature.reviews.LocalReviewsRepository
import com.lugonja.savora.feature.reviews.MealReview
import com.lugonja.savora.feature.settings.AppLanguage
import com.lugonja.savora.feature.settings.AppTheme
import com.lugonja.savora.feature.settings.LocalSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MealDetailParsingTest {
    private val meal = testJson.decodeFromString(JsonObject.serializer(), Fixtures.DETAIL_JSON).toMealDetail()

    @Test
    fun `parses flattened ingredients and skips blanks`() {
        assertEquals(listOf(Ingredient("soy sauce", "3/4 cup"), Ingredient("water", "")), meal.ingredients)
    }

    @Test
    fun `splits instructions into steps without headings`() {
        assertEquals(listOf("Preheat oven to 350F.", "Combine soy sauce and water."), meal.steps)
    }

    @Test
    fun `normalises blank fields and tags`() {
        assertEquals("Teriyaki Chicken Casserole", meal.name)
        assertNull(meal.area)
        assertEquals(listOf("Meat", "Casserole"), meal.tags)
        assertTrue(meal.hasVideo)
        assertFalse(meal.hasSource)
    }
}

class FavoritesRepositoryTest {
    private val repository = LocalFavoritesRepository(InMemoryKeyValueStore(), testJson)

    @Test
    fun `toggle adds to the top and removes again`() = runTest {
        repository.toggle(Fixtures.summary)
        assertEquals(listOf(Fixtures.summary), repository.favorites.first())

        repository.toggle(Fixtures.summary)
        assertEquals(emptyList<MealSummary>(), repository.favorites.first())
    }

    @Test
    fun `undo restores the original position`() = runTest {
        val meals = (1..3).map { MealSummary("$it", "Meal $it", Fixtures.summary.thumbnailUrl) }
        meals.reversed().forEach { repository.add(it) }

        val index = repository.remove("2")
        repository.add(meals[1], index ?: 0)

        assertEquals(listOf("1", "2", "3"), repository.favorites.first().map { it.id })
    }
}

class ReviewsRepositoryTest {
    private val repository = LocalReviewsRepository(InMemoryKeyValueStore(), testJson)

    @Test
    fun `reviews persist and can be deleted`() = runTest {
        val review = MealReview("1", rating = 4, comment = "More garlic", updatedAtMillis = 1L)

        repository.save(review)
        assertEquals(review, repository.review("1").first())

        repository.delete("1")
        assertNull(repository.review("1").first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ratings outside 1 to 5 are rejected`() {
        MealReview("1", rating = 6, comment = "", updatedAtMillis = 1L)
    }
}

class SettingsRepositoryTest {
    private val repository = LocalSettingsRepository(InMemoryKeyValueStore())

    @Test
    fun `first launch defaults to English and the system theme`() = runTest {
        assertEquals(AppLanguage.English, repository.language.first())
        assertEquals(AppTheme.System, repository.theme.first())
    }

    @Test
    fun `an explicit system language choice is remembered`() = runTest {
        repository.setLanguage(AppLanguage.System)

        assertEquals(AppLanguage.System, repository.language.first())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `removing emits an undoable event that restores the recipe`() = runTest(mainRule.dispatcher) {
        val viewModel = FavoritesViewModel(LocalFavoritesRepository(InMemoryKeyValueStore(), testJson))
        val second = Fixtures.summary.copy(id = "2")
        viewModel.toggle(second)
        viewModel.toggle(Fixtures.summary)
        advanceUntilIdle()

        viewModel.removals.test {
            viewModel.remove(second)
            val removal = awaitItem()
            assertEquals(FavoriteRemoved(second, index = 1), removal)
            advanceUntilIdle()
            assertEquals(listOf(Fixtures.summary), viewModel.favorites.value)

            viewModel.undo(removal)
            advanceUntilIdle()
            assertEquals(listOf(Fixtures.summary, second), viewModel.favorites.value)
        }
    }
}

package com.lugonja.savora

import com.lugonja.savora.core.network.Outcome
import com.lugonja.savora.feature.meals.data.MealsRepository
import com.lugonja.savora.feature.meals.model.MealCategory
import com.lugonja.savora.feature.meals.model.MealDetail
import com.lugonja.savora.feature.meals.model.MealSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Routes `Dispatchers.Main` (used by `viewModelScope`) to a test dispatcher. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}

val testJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

class FakeMealsRepository : MealsRepository {
    var categoriesOutcome: Outcome<List<MealCategory>> = Outcome.Success(Fixtures.categories)
    var mealsOutcome: Outcome<List<MealSummary>> = Outcome.Success(listOf(Fixtures.summary))
    val requestedCategories = mutableListOf<String>()
    val searchedQueries = mutableListOf<String>()

    override suspend fun categories(forceRefresh: Boolean) = categoriesOutcome

    override suspend fun mealsInCategory(category: String): Outcome<List<MealSummary>> {
        requestedCategories += category
        return mealsOutcome
    }

    override suspend fun search(query: String): Outcome<List<MealSummary>> {
        searchedQueries += query
        return mealsOutcome
    }

    override suspend fun meal(id: String): Outcome<MealDetail> = Outcome.Success(Fixtures.detail)

    override suspend fun randomMeal(): Outcome<MealDetail> = Outcome.Success(Fixtures.detail)
}

object Fixtures {
    val summary = MealSummary("52772", "Teriyaki Chicken Casserole", "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg")

    val categories = listOf(
        MealCategory("1", "Beef", "https://www.themealdb.com/images/category/beef.png"),
        MealCategory("2", "Chicken", "https://www.themealdb.com/images/category/chicken.png"),
    )

    const val DETAIL_JSON = """
    {
      "idMeal": "52772",
      "strMeal": "  Teriyaki Chicken Casserole ",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg",
      "strCategory": "Chicken",
      "strArea": "",
      "strInstructions": "STEP 1\r\nPreheat oven to 350F.\r\n\r\n2.\r\nCombine soy sauce and water.",
      "strTags": "Meat, Casserole,",
      "strYoutube": "https://www.youtube.com/watch?v=4aZr5hZXP_s",
      "strSource": " ",
      "strIngredient1": "soy sauce",
      "strMeasure1": "3/4 cup",
      "strIngredient2": "water",
      "strMeasure2": null,
      "strIngredient3": "",
      "strMeasure3": ""
    }
    """

    val detail: MealDetail = MealDetail(
        id = "52772",
        name = "Teriyaki Chicken Casserole",
        thumbnailUrl = summary.thumbnailUrl,
        category = "Chicken",
        area = null,
        instructions = "Preheat oven to 350F.",
        ingredients = emptyList(),
        tags = emptyList(),
        youtubeUrl = null,
        sourceUrl = null,
    )

    const val CATEGORIES_JSON =
        """{"categories": [{"idCategory": "1", "strCategory": "Beef", "strCategoryThumb": "https://www.themealdb.com/images/category/beef.png", "strCategoryDescription": "Beef."}]}"""

    const val LISTING_JSON =
        """{"meals": [{"idMeal": "52772", "strMeal": "Teriyaki Chicken Casserole", "strMealThumb": "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg"}]}"""
}

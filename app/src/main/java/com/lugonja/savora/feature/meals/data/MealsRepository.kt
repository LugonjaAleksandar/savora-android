package com.lugonja.savora.feature.meals.data

import com.lugonja.savora.core.network.AppFailure
import com.lugonja.savora.core.network.NotFoundException
import com.lugonja.savora.core.network.Outcome
import com.lugonja.savora.core.network.safeApiCall
import com.lugonja.savora.feature.meals.model.MealCategory
import com.lugonja.savora.feature.meals.model.MealDetail
import com.lugonja.savora.feature.meals.model.MealSummary
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration.Companion.days

interface MealsRepository {
    suspend fun categories(forceRefresh: Boolean = false): Outcome<List<MealCategory>>

    suspend fun mealsInCategory(category: String): Outcome<List<MealSummary>>

    suspend fun search(query: String): Outcome<List<MealSummary>>

    suspend fun meal(id: String): Outcome<MealDetail>

    suspend fun randomMeal(): Outcome<MealDetail>
}

/**
 * Caching strategy:
 * - categories and meal details change rarely, so they are **cache-first** with a TTL;
 * - category listings are **network-first**, falling back to the last cached copy when offline;
 * - search is network only (results are cheap and query-specific).
 */
class DefaultMealsRepository(
    private val api: MealDbApi,
    private val cache: MealsCache,
) : MealsRepository {

    override suspend fun categories(forceRefresh: Boolean): Outcome<List<MealCategory>> {
        val key = "categories"
        val serializer = ListSerializer(MealCategory.serializer())
        if (!forceRefresh) {
            cache.read(key, serializer, maxAge = CATEGORIES_TTL)?.let { return Outcome.Success(it) }
        }
        return fetchAndCache(key, serializer) { api.categories().categories.orEmpty() }
    }

    override suspend fun mealsInCategory(category: String): Outcome<List<MealSummary>> =
        fetchAndCache("category_$category", ListSerializer(MealSummary.serializer())) { api.mealsByCategory(category).meals.orEmpty() }

    override suspend fun search(query: String): Outcome<List<MealSummary>> = safeApiCall { api.search(query).meals.orEmpty() }

    override suspend fun meal(id: String): Outcome<MealDetail> {
        val key = "meal_$id"
        cache.read(key, MealDetail.serializer(), maxAge = MEAL_DETAIL_TTL)?.let { return Outcome.Success(it) }
        return fetchAndCache(key, MealDetail.serializer()) { api.lookup(id).firstMeal() }
    }

    /** Warms the detail cache, so opening the random pick right after is instant. */
    override suspend fun randomMeal(): Outcome<MealDetail> {
        val outcome = safeApiCall { api.random().firstMeal() }
        if (outcome is Outcome.Success) {
            cache.write("meal_${outcome.data.id}", outcome.data, MealDetail.serializer())
        }
        return outcome
    }

    /**
     * On success persists the value; on a network failure serves whatever is
     * cached (ignoring the TTL), so the app stays usable offline.
     */
    private suspend fun <T> fetchAndCache(key: String, serializer: KSerializer<T>, fetch: suspend () -> T): Outcome<T> =
        when (val outcome = safeApiCall(fetch)) {
            is Outcome.Success -> outcome.also { cache.write(key, it.data, serializer) }
            is Outcome.Failure -> {
                val stale = if (outcome.failure == AppFailure.Network) cache.read(key, serializer, maxAge = null) else null
                stale?.let { Outcome.Success(it) } ?: outcome
            }
        }

    private fun MealsResponse<JsonObject>.firstMeal(): MealDetail =
        meals?.firstOrNull()?.toMealDetail() ?: throw NotFoundException("Meal not found")

    companion object {
        val CATEGORIES_TTL = 1.days
        val MEAL_DETAIL_TTL = 7.days
    }
}

package com.lugonja.savora.feature.meals.data

import com.lugonja.savora.feature.meals.model.Ingredient
import com.lugonja.savora.feature.meals.model.MealCategory
import com.lugonja.savora.feature.meals.model.MealDetail
import com.lugonja.savora.feature.meals.model.MealSummary
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.http.GET
import retrofit2.http.Query

/** TheMealDB endpoints. "No results" arrives as `{"meals": null}`, hence the nullable lists. */
interface MealDbApi {
    @GET("categories.php")
    suspend fun categories(): CategoriesResponse

    @GET("filter.php")
    suspend fun mealsByCategory(@Query("c") category: String): MealsResponse<MealSummary>

    @GET("search.php")
    suspend fun search(@Query("s") query: String): MealsResponse<MealSummary>

    @GET("lookup.php")
    suspend fun lookup(@Query("i") id: String): MealsResponse<JsonObject>

    @GET("random.php")
    suspend fun random(): MealsResponse<JsonObject>
}

@Serializable
data class CategoriesResponse(val categories: List<MealCategory>? = null)

@Serializable
data class MealsResponse<T>(val meals: List<T>? = null)

/** TheMealDB flattens ingredients into `strIngredient1..20` / `strMeasure1..20`. */
private const val MAX_INGREDIENTS = 20

fun JsonObject.toMealDetail(): MealDetail {
    fun text(key: String): String? = (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }?.contentOrNull?.trim()?.ifEmpty { null }

    return MealDetail(
        id = requireNotNull(text("idMeal")) { "Meal without id" },
        name = requireNotNull(text("strMeal")) { "Meal without name" },
        thumbnailUrl = requireNotNull(text("strMealThumb")) { "Meal without thumbnail" },
        category = text("strCategory"),
        area = text("strArea"),
        instructions = text("strInstructions").orEmpty(),
        ingredients = (1..MAX_INGREDIENTS).mapNotNull { index ->
            text("strIngredient$index")?.let { Ingredient(name = it, measure = text("strMeasure$index").orEmpty()) }
        },
        tags = text("strTags").orEmpty().split(',').map(String::trim).filter(String::isNotEmpty),
        youtubeUrl = text("strYoutube"),
        sourceUrl = text("strSource"),
    )
}

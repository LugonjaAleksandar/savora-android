package com.lugonja.savora.feature.meals.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URLEncoder

/** Lightweight meal used by lists, favorites and as the placeholder while [MealDetail] loads. */
@Serializable
data class MealSummary(
    @SerialName("idMeal") val id: String,
    @SerialName("strMeal") val name: String,
    @SerialName("strMealThumb") val thumbnailUrl: String,
) {
    /** TheMealDB serves resized variants at `/medium`, several times lighter for grids. */
    val previewUrl: String get() = "$thumbnailUrl/medium"
}

@Serializable
data class MealCategory(
    @SerialName("idCategory") val id: String,
    @SerialName("strCategory") val name: String,
    @SerialName("strCategoryThumb") val thumbnailUrl: String,
    @SerialName("strCategoryDescription") val description: String = "",
)

@Serializable
data class Ingredient(val name: String, val measure: String) {
    val imageUrl: String
        get() = "https://www.themealdb.com/images/ingredients/${URLEncoder.encode(name, "UTF-8").replace("+", "%20")}-small.png"
}

@Serializable
data class MealDetail(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val category: String?,
    val area: String?,
    val instructions: String,
    val ingredients: List<Ingredient>,
    val tags: List<String>,
    val youtubeUrl: String?,
    val sourceUrl: String?,
) {
    val summary: MealSummary get() = MealSummary(id, name, thumbnailUrl)

    /** Instructions split into readable steps, dropping "STEP 1" style heading lines. */
    val steps: List<String>
        get() = instructions.lines().map(String::trim).filter { it.isNotEmpty() && !STEP_HEADING.matches(it) }

    val hasVideo: Boolean get() = youtubeUrl.isHttpUrl()

    val hasSource: Boolean get() = sourceUrl.isHttpUrl()
}

private val STEP_HEADING = Regex("""(?i)(step\s*\d+|\d+\.?)""")

private fun String?.isHttpUrl(): Boolean = this?.startsWith("http") == true

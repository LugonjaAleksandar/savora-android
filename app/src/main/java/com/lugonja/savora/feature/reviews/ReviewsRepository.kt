package com.lugonja.savora.feature.reviews

import com.lugonja.savora.core.storage.KeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class MealReview(
    val mealId: String,
    val rating: Int,
    val comment: String,
    val updatedAtMillis: Long,
) {
    init {
        require(rating in RATING_RANGE) { "Rating must be between 1 and 5" }
    }

    companion object {
        val RATING_RANGE = 1..5
    }
}

interface ReviewsRepository {
    fun review(mealId: String): Flow<MealReview?>

    suspend fun save(review: MealReview)

    suspend fun delete(mealId: String)
}

/** Reviews live on-device only. Swapping this for a remote implementation would not touch the UI. */
class LocalReviewsRepository(
    private val store: KeyValueStore,
    private val json: Json,
) : ReviewsRepository {

    override fun review(mealId: String): Flow<MealReview?> = store.observe(KEY_PREFIX + mealId).map { raw ->
        try {
            raw?.let { json.decodeFromString(MealReview.serializer(), it) }
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override suspend fun save(review: MealReview) {
        store.set(KEY_PREFIX + review.mealId, json.encodeToString(MealReview.serializer(), review))
    }

    override suspend fun delete(mealId: String) {
        store.set(KEY_PREFIX + mealId, null)
    }

    private companion object {
        const val KEY_PREFIX = "review_"
    }
}

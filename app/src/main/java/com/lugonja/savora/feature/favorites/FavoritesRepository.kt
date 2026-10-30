package com.lugonja.savora.feature.favorites

import com.lugonja.savora.core.storage.KeyValueStore
import com.lugonja.savora.feature.meals.model.MealSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Stores the full [MealSummary] (not just the id), so the Saved tab renders
 * instantly and offline. App-wide, because the heart appears on grid cards,
 * the detail screen and the Saved tab at the same time.
 */
interface FavoritesRepository {
    val favorites: Flow<List<MealSummary>>

    suspend fun toggle(meal: MealSummary)

    /** New favorites go to the top; [index] restores a removed one to where it was. */
    suspend fun add(meal: MealSummary, index: Int = 0)

    /** Returns the removed position, so the caller can offer an undo. */
    suspend fun remove(mealId: String): Int?
}

class LocalFavoritesRepository(
    private val store: KeyValueStore,
    private val json: Json,
) : FavoritesRepository {

    private val serializer = ListSerializer(MealSummary.serializer())

    override val favorites: Flow<List<MealSummary>> = store.observe(KEY).map(::decode)

    override suspend fun toggle(meal: MealSummary) {
        if (current().any { it.id == meal.id }) remove(meal.id) else add(meal)
    }

    override suspend fun add(meal: MealSummary, index: Int) {
        val favorites = current()
        if (favorites.any { it.id == meal.id }) return
        save(favorites.toMutableList().apply { add(index.coerceIn(0, size), meal) })
    }

    override suspend fun remove(mealId: String): Int? {
        val favorites = current()
        val index = favorites.indexOfFirst { it.id == mealId }.takeIf { it >= 0 } ?: return null
        save(favorites.filterNot { it.id == mealId })
        return index
    }

    private suspend fun current(): List<MealSummary> = favorites.first()

    private suspend fun save(favorites: List<MealSummary>) {
        store.set(KEY, json.encodeToString(serializer, favorites))
    }

    private fun decode(raw: String?): List<MealSummary> = try {
        raw?.let { json.decodeFromString(serializer, it) }.orEmpty()
    } catch (_: SerializationException) {
        emptyList()
    }

    private companion object {
        const val KEY = "favorites"
    }
}

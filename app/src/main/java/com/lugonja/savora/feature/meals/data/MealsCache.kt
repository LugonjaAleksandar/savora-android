package com.lugonja.savora.feature.meals.data

import com.lugonja.savora.core.storage.KeyValueStore
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration

/** JSON cache with a timestamp per entry, layered over [KeyValueStore]. */
class MealsCache(
    private val store: KeyValueStore,
    private val json: Json,
    private val now: () -> Long = System::currentTimeMillis,
) {
    suspend fun <T> write(key: String, value: T, serializer: KSerializer<T>) {
        val entry = CacheEntry(savedAt = now(), data = json.encodeToJsonElement(serializer, value))
        store.set(KEY_PREFIX + key, json.encodeToString(CacheEntry.serializer(), entry))
    }

    /**
     * Returns the cached value, or `null` when it is missing, corrupt or older
     * than [maxAge]. Passing `maxAge = null` accepts stale data (offline fallback).
     */
    suspend fun <T> read(key: String, serializer: KSerializer<T>, maxAge: Duration?): T? {
        val raw = store.get(KEY_PREFIX + key) ?: return null
        return try {
            val entry = json.decodeFromString(CacheEntry.serializer(), raw)
            if (maxAge != null && now() - entry.savedAt > maxAge.inWholeMilliseconds) null else json.decodeFromJsonElement(serializer, entry.data)
        } catch (_: SerializationException) {
            store.set(KEY_PREFIX + key, null)
            null
        } catch (_: IllegalArgumentException) {
            store.set(KEY_PREFIX + key, null)
            null
        }
    }

    @Serializable
    private data class CacheEntry(val savedAt: Long, val data: JsonElement)

    private companion object {
        const val KEY_PREFIX = "meals_cache_"
    }
}

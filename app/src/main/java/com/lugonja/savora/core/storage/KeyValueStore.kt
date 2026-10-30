package com.lugonja.savora.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Minimal persistence contract. Repositories depend on this instead of
 * DataStore, so tests run against [InMemoryKeyValueStore].
 */
interface KeyValueStore {
    fun observe(key: String): Flow<String?>

    suspend fun get(key: String): String? = observe(key).first()

    suspend fun set(key: String, value: String?)
}

class DataStoreKeyValueStore(private val dataStore: DataStore<Preferences>) : KeyValueStore {
    override fun observe(key: String): Flow<String?> = dataStore.data.map { it[stringPreferencesKey(key)] }

    override suspend fun set(key: String, value: String?) {
        dataStore.edit { preferences ->
            val preferenceKey = stringPreferencesKey(key)
            if (value == null) preferences.remove(preferenceKey) else preferences[preferenceKey] = value
        }
    }
}

class InMemoryKeyValueStore : KeyValueStore {
    private val values = MutableStateFlow<Map<String, String>>(emptyMap())

    override fun observe(key: String): Flow<String?> = values.map { it[key] }

    override suspend fun set(key: String, value: String?) {
        values.update { if (value == null) it - key else it + (key to value) }
    }
}

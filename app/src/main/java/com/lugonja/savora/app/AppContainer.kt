package com.lugonja.savora.app

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.lugonja.savora.core.storage.DataStoreKeyValueStore
import com.lugonja.savora.core.storage.KeyValueStore
import com.lugonja.savora.feature.favorites.FavoritesRepository
import com.lugonja.savora.feature.favorites.LocalFavoritesRepository
import com.lugonja.savora.feature.meals.data.DefaultMealsRepository
import com.lugonja.savora.feature.meals.data.MealDbApi
import com.lugonja.savora.feature.meals.data.MealsCache
import com.lugonja.savora.feature.meals.data.MealsRepository
import com.lugonja.savora.feature.reviews.LocalReviewsRepository
import com.lugonja.savora.feature.reviews.ReviewsRepository
import com.lugonja.savora.feature.settings.LocalSettingsRepository
import com.lugonja.savora.feature.settings.SettingsRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "savora")

/**
 * Composition root: the only place concrete implementations are chosen.
 * Everything else receives its dependencies through constructors, the same
 * shape as the manual-DI guide on developer.android.com.
 */
class AppContainer(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val store: KeyValueStore = DataStoreKeyValueStore(context.applicationContext.dataStore)

    private val api: MealDbApi = Retrofit.Builder()
        .baseUrl(MEAL_DB_BASE_URL)
        .client(OkHttpClient.Builder().callTimeout(15, TimeUnit.SECONDS).build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(MealDbApi::class.java)

    val mealsRepository: MealsRepository = DefaultMealsRepository(api, MealsCache(store, json))
    val reviewsRepository: ReviewsRepository = LocalReviewsRepository(store, json)
    val favoritesRepository: FavoritesRepository = LocalFavoritesRepository(store, json)
    val settingsRepository: SettingsRepository = LocalSettingsRepository(store)

    companion object {
        const val MEAL_DB_BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    }
}

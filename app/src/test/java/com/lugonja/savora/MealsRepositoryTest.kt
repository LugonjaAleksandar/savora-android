package com.lugonja.savora

import com.lugonja.savora.core.network.AppFailure
import com.lugonja.savora.core.network.Outcome
import com.lugonja.savora.core.storage.InMemoryKeyValueStore
import com.lugonja.savora.feature.meals.data.DefaultMealsRepository
import com.lugonja.savora.feature.meals.data.MealDbApi
import com.lugonja.savora.feature.meals.data.MealsCache
import com.lugonja.savora.feature.meals.model.MealCategory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/** Real Retrofit + JSON stack against MockWebServer, with a controllable clock. */
class MealsRepositoryTest {
    private val server = MockWebServer()
    private val store = InMemoryKeyValueStore()
    private var now = 1_700_000_000_000L
    private lateinit var repository: DefaultMealsRepository

    @Before
    fun setUp() {
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/api/json/v1/1/"))
            .addConverterFactory(testJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MealDbApi::class.java)
        repository = DefaultMealsRepository(api, MealsCache(store, testJson) { now })
    }

    @After
    fun tearDown() = server.shutdown()

    /** A stopped server refuses connections, the same failure a device without network sees. */
    private fun goOffline() = server.shutdown()

    @Test
    fun `builds the query and decodes listings`() = runTest {
        server.enqueue(MockResponse().setBody(Fixtures.LISTING_JSON))

        val outcome = repository.mealsInCategory("Sea food")

        assertEquals(Outcome.Success(listOf(Fixtures.summary)), outcome)
        assertEquals("/api/json/v1/1/filter.php?c=Sea%20food", server.takeRequest().path)
    }

    @Test
    fun `maps HTTP and transport errors to typed failures`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setBody("<html>"))

        assertEquals(Outcome.Failure(AppFailure.NotFound), repository.search("a"))
        assertEquals(Outcome.Failure(AppFailure.Server(503)), repository.search("b"))
        assertEquals(Outcome.Failure(AppFailure.Parsing), repository.search("c"))
        goOffline()
        assertEquals(Outcome.Failure(AppFailure.Network), repository.search("d"))
    }

    @Test
    fun `categories are served from cache within the TTL and refetched after it`() = runTest {
        server.enqueue(MockResponse().setBody(Fixtures.CATEGORIES_JSON))
        server.enqueue(MockResponse().setBody(Fixtures.CATEGORIES_JSON))

        repository.categories()
        now += 1.hours.inWholeMilliseconds
        repository.categories()
        assertEquals(1, server.requestCount)

        now += 1.days.inWholeMilliseconds
        repository.categories()
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `category listing falls back to stale cache when offline`() = runTest {
        server.enqueue(MockResponse().setBody(Fixtures.LISTING_JSON))

        repository.mealsInCategory("Chicken")
        now += 30.days.inWholeMilliseconds
        goOffline()

        assertEquals(Outcome.Success(listOf(Fixtures.summary)), repository.mealsInCategory("Chicken"))
    }

    @Test
    fun `server errors are not masked by the cache`() = runTest {
        server.enqueue(MockResponse().setBody(Fixtures.LISTING_JSON))
        server.enqueue(MockResponse().setResponseCode(500))

        repository.mealsInCategory("Chicken")

        assertEquals(Outcome.Failure(AppFailure.Server(500)), repository.mealsInCategory("Chicken"))
    }

    @Test
    fun `random meal warms the detail cache and unknown ids are not found`() = runTest {
        server.enqueue(MockResponse().setBody("""{"meals": [${Fixtures.DETAIL_JSON}]}"""))
        server.enqueue(MockResponse().setBody("""{"meals": null}"""))

        val random = (repository.randomMeal() as Outcome.Success).data
        assertEquals(Outcome.Success(random), repository.meal(random.id))
        assertEquals(1, server.requestCount)

        assertEquals(Outcome.Failure(AppFailure.NotFound), repository.meal("0"))
    }

    @Test
    fun `corrupt cache entries are dropped`() = runTest {
        store.set("meals_cache_categories", "not json")

        assertNull(MealsCache(store, testJson).read("categories", ListSerializer(MealCategory.serializer()), maxAge = null))
        assertNull(store.get("meals_cache_categories"))
    }
}

package com.lugonja.savora.core.network

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "SafeApiCall"

/**
 * Runs a Retrofit call and maps every failure mode to an [AppFailure].
 * Cancellation is rethrown, so structured concurrency keeps working.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): Outcome<T> = try {
    Outcome.Success(call())
} catch (error: CancellationException) {
    throw error
} catch (_: NotFoundException) {
    Outcome.Failure(AppFailure.NotFound)
} catch (error: HttpException) {
    Outcome.Failure(if (error.code() == 404) AppFailure.NotFound else AppFailure.Server(error.code()))
} catch (error: SerializationException) {
    Log.w(TAG, "Unexpected payload shape", error)
    Outcome.Failure(AppFailure.Parsing)
} catch (error: IllegalArgumentException) {
    Log.w(TAG, "Unexpected payload shape", error)
    Outcome.Failure(AppFailure.Parsing)
} catch (error: IOException) {
    Outcome.Failure(AppFailure.Network)
}

/** Thrown inside [safeApiCall] when a successful response holds no matching item. */
class NotFoundException(message: String) : RuntimeException(message)

package com.lugonja.savora.core.network

/**
 * Every way a request can fail, as a closed set. The UI maps each one to
 * localized copy, never to a raw exception message.
 */
sealed interface AppFailure {
    data object Network : AppFailure
    data class Server(val statusCode: Int) : AppFailure
    data object NotFound : AppFailure
    data object Parsing : AppFailure
    data object Unknown : AppFailure
}

/** Result of a data operation. `when` over it is exhaustive, unlike try/catch. */
sealed interface Outcome<out T> {
    data class Success<out T>(val data: T) : Outcome<T>
    data class Failure(val failure: AppFailure) : Outcome<Nothing>
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Failure -> this
}

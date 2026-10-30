package com.lugonja.savora.core.ui

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.lugonja.savora.R
import com.lugonja.savora.core.network.AppFailure
import java.text.DateFormat
import java.util.Date

/**
 * Android selects plurals by CLDR category only, so the "exactly zero" copy
 * from the shared ARB files lives in a separate `*_zero` string.
 */
@Composable
fun quantityString(@PluralsRes plurals: Int, count: Int, @StringRes zero: Int? = null): String =
    if (count == 0 && zero != null) stringResource(zero) else pluralStringResource(plurals, count, count)

@get:StringRes
val AppFailure.messageRes: Int
    get() = when (this) {
        AppFailure.Network -> R.string.error_network
        is AppFailure.Server -> R.string.error_server
        AppFailure.NotFound -> R.string.error_not_found
        AppFailure.Parsing, AppFailure.Unknown -> R.string.error_unknown
    }

@Composable
fun AppFailure.message(): String = stringResource(messageRes)

@Composable
fun ratingLabel(rating: Int): String = stringResource(
    when (rating) {
        1 -> R.string.review_rating_label_1
        2 -> R.string.review_rating_label_2
        3 -> R.string.review_rating_label_3
        4 -> R.string.review_rating_label_4
        else -> R.string.review_rating_label_5
    },
)

@Composable
fun formatDate(millis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(millis))
}

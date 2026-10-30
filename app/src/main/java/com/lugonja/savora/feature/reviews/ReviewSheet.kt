package com.lugonja.savora.feature.reviews

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.designsystem.pressable
import com.lugonja.savora.core.ui.ratingLabel

@Composable
fun ReviewSheet(initialReview: MealReview?, onSave: (rating: Int, comment: String) -> Unit) {
    val colors = SavoraTheme.colors
    val haptics = LocalHapticFeedback.current
    var rating by rememberSaveable { mutableIntStateOf(initialReview?.rating ?: 0) }
    var comment by rememberSaveable { mutableStateOf(initialReview?.comment.orEmpty()) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(stringResource(R.string.review_sheet_title), style = SavoraTheme.display(28), color = colors.textPrimary)

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            for (star in MealReview.RATING_RANGE) {
                val scale by animateFloatAsState(if (star == rating) 1.15f else 1f, spring(dampingRatio = 0.45f), label = "starScale")
                Icon(
                    imageVector = if (star <= rating) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                    contentDescription = "$star",
                    tint = if (star <= rating) colors.star else colors.textTertiary,
                    modifier = Modifier
                        .size(46.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .pressable(scale = 0.85f) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            rating = star
                        },
                )
            }
        }

        AnimatedContent(targetState = rating, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "ratingLabel") { value ->
            Text(
                text = if (value == 0) " " else ratingLabel(value),
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary,
            )
        }

        TextField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = { Text(stringResource(R.string.review_comment_hint), color = colors.textTertiary) },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(Radius.md),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceRaised,
                unfocusedContainerColor = colors.surfaceRaised,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = colors.primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .graphicsLayer { alpha = if (rating == 0) 0.5f else 1f }
                .background(colors.brandGradient, CircleShape)
                .pressable { if (rating > 0) onSave(rating, comment) },
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.review_save), color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

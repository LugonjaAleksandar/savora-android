package com.lugonja.savora.feature.reviews

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.GradientButton
import com.lugonja.savora.core.designsystem.Radius
import com.lugonja.savora.core.designsystem.RatingStars
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.ui.formatDate
import com.lugonja.savora.core.ui.ratingLabel

/** Either a call to action or the saved review. Editing and deleting are reported to the route. */
@Composable
fun ReviewSection(review: MealReview?, onWrite: () -> Unit, onDelete: () -> Unit) {
    AnimatedContent(
        targetState = review,
        transitionSpec = { (fadeIn() + scaleIn(spring(dampingRatio = 0.9f), initialScale = 0.97f)) togetherWith fadeOut() },
        contentKey = { it != null },
        label = "review",
    ) { current ->
        if (current == null) ReviewCallToAction(onWrite) else SavedReviewCard(current, onEdit = onWrite, onDelete = onDelete)
    }
}

@Composable
private fun ReviewCallToAction(onWrite: () -> Unit) {
    val colors = SavoraTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.brandGradient, RoundedCornerShape(Radius.lg))
            .padding(1.5.dp)
            .background(colors.surface, RoundedCornerShape(Radius.lg - 1.5.dp))
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(stringResource(R.string.review_cta_title), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Text(stringResource(R.string.review_cta_subtitle), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        GradientButton(
            text = stringResource(R.string.review_cta_button),
            icon = Icons.Rounded.Star,
            onClick = onWrite,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun SavedReviewCard(review: MealReview, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = SavoraTheme.colors
    val shape = RoundedCornerShape(Radius.lg)
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.stroke, shape)
            .padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.lg, bottom = Spacing.sm),
    ) {
        Row(Modifier.padding(end = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.review_section_title), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            RatingStars(review.rating, size = 22.dp)
        }
        Text(ratingLabel(review.rating), style = MaterialTheme.typography.titleSmall, color = colors.primary, modifier = Modifier.padding(top = Spacing.xs))
        if (review.comment.isNotEmpty()) {
            Text(
                "“${review.comment}”",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = colors.textPrimary,
                modifier = Modifier.padding(top = Spacing.md, end = Spacing.md),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Spacing.sm)) {
            Text(
                stringResource(R.string.review_updated_at, formatDate(review.updatedAtMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.review_edit), modifier = Modifier.padding(start = Spacing.xs))
            }
            TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = colors.danger)) {
                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.review_delete), modifier = Modifier.padding(start = Spacing.xs))
            }
        }
    }
}

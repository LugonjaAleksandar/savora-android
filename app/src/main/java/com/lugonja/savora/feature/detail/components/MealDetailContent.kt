package com.lugonja.savora.feature.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lugonja.savora.R
import com.lugonja.savora.core.designsystem.SavoraTheme
import com.lugonja.savora.core.designsystem.SectionTitle
import com.lugonja.savora.core.designsystem.Spacing
import com.lugonja.savora.core.ui.quantityString
import com.lugonja.savora.feature.meals.model.MealDetail
import com.lugonja.savora.feature.reviews.MealReview
import com.lugonja.savora.feature.reviews.ReviewSection

/** The loaded recipe. Links and the review sheet are reported upwards to the route. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealDetailContent(
    meal: MealDetail,
    review: MealReview?,
    onWriteReview: () -> Unit,
    onDeleteReview: () -> Unit,
    onOpenLink: (String) -> Unit,
) {
    val colors = SavoraTheme.colors
    Column(Modifier.fillMaxWidth().padding(bottom = Spacing.xl)) {
        Text(meal.name, style = SavoraTheme.display(30), color = colors.textPrimary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.lg),
        ) {
            meal.category?.let { MetaChip(Icons.Rounded.RestaurantMenu, it) }
            meal.area?.let { MetaChip(Icons.Rounded.Public, it) }
            MetaChip(Icons.Rounded.ShoppingBasket, quantityString(R.plurals.detail_ingredients_count, meal.ingredients.size))
            meal.tags.forEach { MetaChip(Icons.Rounded.Tag, it) }
        }

        ReviewSection(review = review, onWrite = onWriteReview, onDelete = onDeleteReview)

        SectionTitle(stringResource(R.string.detail_ingredients), trailing = "${meal.ingredients.size}", modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.md))
        // Non-lazy grid: it lives inside the screen's LazyColumn and has at most 20 items.
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            meal.ingredients.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    row.forEach { IngredientTile(it, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        SectionTitle(stringResource(R.string.detail_instructions), modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.md))
        InstructionSteps(meal.steps)

        if (meal.hasVideo || meal.hasSource) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.padding(top = Spacing.sm)) {
                meal.youtubeUrl?.takeIf { meal.hasVideo }?.let { url ->
                    Button(
                        onClick = { onOpenLink(url) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0033)),
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) {
                        Icon(Icons.Rounded.SmartDisplay, contentDescription = null)
                        Text(stringResource(R.string.detail_watch_video), modifier = Modifier.padding(start = Spacing.sm), maxLines = 1)
                    }
                }
                meal.sourceUrl?.takeIf { meal.hasSource }?.let { url ->
                    OutlinedButton(onClick = { onOpenLink(url) }, modifier = Modifier.weight(1f).height(52.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null)
                        Text(stringResource(R.string.detail_source), modifier = Modifier.padding(start = Spacing.sm), maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaChip(icon: ImageVector, label: String) {
    val colors = SavoraTheme.colors
    Row(
        Modifier
            .background(colors.surfaceRaised, CircleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
    }
}

@Composable
private fun InstructionSteps(steps: List<String>) {
    val colors = SavoraTheme.colors
    Column {
        steps.forEachIndexed { index, step ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(34.dp).background(colors.brandGradient, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                    if (index < steps.lastIndex) {
                        Box(
                            Modifier
                                .padding(vertical = Spacing.xs)
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.5f), colors.stroke)), CircleShape),
                        )
                    }
                }
                Column(Modifier.weight(1f).padding(top = 6.dp, bottom = Spacing.lg)) {
                    Text(
                        stringResource(R.string.detail_step, index + 1),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.primary,
                    )
                    Text(step, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary, modifier = Modifier.padding(top = Spacing.xs))
                }
            }
        }
    }
}

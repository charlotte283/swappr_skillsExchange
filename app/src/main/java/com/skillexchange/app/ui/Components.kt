package com.skillexchange.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skillexchange.app.data.Provider
import com.skillexchange.app.data.Service
import com.skillexchange.app.data.formatValue
import com.skillexchange.app.ml.Factor
import java.util.Locale

val DefaultCardShape = RoundedCornerShape(20.dp)
val DefaultCardBorder = BorderStroke(1.dp, Color(0xFF334155))

@Composable
fun marketplaceCardColors(containerColor: Color = Color(0xFF1E293B)): CardColors =
    CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = Color(0xFFF8FAFC)
    )

@Composable
fun marketplaceTextFieldColors(): TextFieldColors =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFF1E293B),
        unfocusedContainerColor = Color(0xFF0F172A),
        disabledContainerColor = Color(0xFF1E293B),
        focusedBorderColor = Color(0xFF34D399),
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = Color(0xFF34D399),
        unfocusedLabelColor = Color(0xFF94A3B8),
        focusedTextColor = Color(0xFFF8FAFC),
        unfocusedTextColor = Color(0xFFF8FAFC),
        focusedPlaceholderColor = Color(0xFF94A3B8),
        unfocusedPlaceholderColor = Color(0xFF64748B)
    )

@Composable
fun MarketplaceCard(
    modifier: Modifier = Modifier,
    shape: Shape = DefaultCardShape,
    border: BorderStroke? = DefaultCardBorder,
    colors: CardColors = marketplaceCardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        border = border,
        elevation = elevation,
        content = content
    )
}

/** 2-Column Staggered Grid Card for Home & Search screens */
@Composable
fun ServiceGridCard(
    service: Service,
    provider: Provider?,
    isFav: Boolean,
    onToggleFav: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MarketplaceCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Column {
            // Image with top floating favourite & category badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFF0F172A))
            ) {
                if (service.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = service.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }

                // Category pill on top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF065F46).copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = service.subcat.ifBlank { service.category },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD1FAE5),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favourite Heart Button on top-right
                IconButton(
                    onClick = onToggleFav,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFav) Color(0xFFEF4444) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Card Body Details
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = service.title.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Provider avatar & name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF065F46)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (provider?.avatarUrl?.isNotBlank() == true) {
                            AsyncImage(
                                model = provider.avatarUrl,
                                contentDescription = provider.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFD1FAE5),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = provider?.name ?: "Provider",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price Value & Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatValue(service.valueEur).split("≈").last().trim(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", provider?.rating ?: 5.0),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            colors = marketplaceTextFieldColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); open = false })
            }
        }
    }
}

@Composable
fun RatingText(rating: Double, reviews: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.padding(end = 2.dp).size(16.dp)
        )
        Text(
            String.format(Locale.US, "%.1f (%d reviews)", rating, reviews),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8)
        )
    }
}

/** "Estimated value" line plus the biggest reasons behind it (from the Ridge coefficients). */
@Composable
fun ValueBlock(valueEur: Double, factors: List<Factor> = emptyList(), isRidge: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Estimated value", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
        Text(formatValue(valueEur), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
        if (!isRidge) {
            Text("Rough estimate (category median).", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        }
        factors.forEach {
            val sign = if (it.effectPercent >= 0) "+" else ""
            Text(
                String.format(Locale.US, "• %s: %s%.0f%%", it.label, sign, it.effectPercent),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun ReasonChips(reasons: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        reasons.take(2).forEach {
            AssistChip(
                onClick = {},
                label = { Text(it, style = MaterialTheme.typography.labelSmall) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF065F46),
                    labelColor = Color(0xFFD1FAE5)
                ),
                border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color(0xFF047857))
            )
        }
    }
}

/** Clickable Swappr Header Logo that navigates back to Home / Explore page when clicked. */
@Composable
fun SwapprHeaderLogo(
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onNavigateHome() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF34D399)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swappr Logo",
                tint = Color(0xFF064E3B),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = "swappr",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

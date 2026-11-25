package com.skillexchange.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.skillexchange.app.data.formatKes
import com.skillexchange.app.data.formatValue
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExchangeScreen(
    vm: AppViewModel,
    serviceId: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {}
) {
    val theirs = vm.serviceById(serviceId)
    val provider = theirs?.let { vm.providerById(it.ownerId) }
    if (theirs == null || provider == null) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This service is no longer available.", style = MaterialTheme.typography.titleMedium, color = Color(0xFFF8FAFC))
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack) { Text("Go Back") }
        }
        return
    }

    val isFav = vm.isFavourite(theirs.id)
    var swapMode by remember { mutableStateOf(true) }

    // Smartly order user skills so skills matching the advertiser request come first
    val sortedMyServices = remember(vm.myServices, provider.wants) {
        vm.myServices.sortedByDescending { s ->
            provider.wants.any { w -> s.category.contains(w, ignoreCase = true) || s.subcat.contains(w, ignoreCase = true) }
        }
    }

    var chosenId by remember(theirs.id) {
        mutableStateOf(sortedMyServices.firstOrNull()?.id)
    }
    var isExpandedDescription by remember { mutableStateOf(false) }

    val mine = vm.myServices.firstOrNull { it.id == chosenId }
    val result = if (mine != null) vm.check(mine, theirs) else null

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Header Image with Back & Favourite Buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF1E293B))
            ) {
                if (theirs.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = theirs.imageUrl,
                        contentDescription = theirs.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }

                // Top Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(40.dp)
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF8FAFC)
                    )
                }

                // Top Favourite Button
                IconButton(
                    onClick = { vm.toggleFavourite(theirs.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFav) Color(0xFFEF4444) else Color(0xFF94A3B8)
                    )
                }
            }

            // Main Content
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category & Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF065F46))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = theirs.subcat.ifBlank { theirs.category },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1FAE5)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f (%d reviews)", provider.rating, provider.reviews),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                }

                // Service Title
                Text(
                    text = theirs.title.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC)
                )

                // Value Estimate
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Estimated Value: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = formatValue(theirs.valueEur),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                }

                // Provider Profile Card
                MarketplaceCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF065F46)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (provider.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = provider.avatarUrl,
                                    contentDescription = provider.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize().clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFFD1FAE5),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(
                                text = provider.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = provider.area,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = provider.bio,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Description & Read More
                Column {
                    Text(
                        text = "Service Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Offering professional ${theirs.subcat} under ${theirs.category}. ${provider.bio} Quality guaranteed with fair value matching based on community historical benchmarks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        maxLines = if (isExpandedDescription) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isExpandedDescription) "Read Less" else "Read More",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        modifier = Modifier
                            .clickable { isExpandedDescription = !isExpandedDescription }
                            .padding(vertical = 4.dp)
                    )
                }

                // What Provider Wants in Exchange
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${provider.name} is looking to learn/receive:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        provider.wants.forEach { want ->
                            AssistChip(
                                onClick = {},
                                label = { Text(want) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF065F46),
                                    labelColor = Color(0xFFD1FAE5)
                                ),
                                border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color(0xFF047857))
                            )
                        }
                    }
                }

                // Exchange Mode Toggle (Swap vs Cash)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = swapMode,
                        onClick = { swapMode = true },
                        label = { Text("Swap Skills") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF34D399),
                            selectedLabelColor = Color(0xFF064E3B),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFF8FAFC)
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = swapMode, borderColor = Color(0xFF334155))
                    )
                    FilterChip(
                        selected = !swapMode,
                        onClick = { swapMode = false },
                        label = { Text("Pay Cash") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF34D399),
                            selectedLabelColor = Color(0xFF064E3B),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFF8FAFC)
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = !swapMode, borderColor = Color(0xFF334155))
                    )
                }

                if (!swapMode) {
                    MarketplaceCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Suggested Price", style = MaterialTheme.typography.titleSmall, color = Color(0xFF94A3B8))
                            Text(formatKes(theirs.valueEur), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                            Text("Agree the final amount with ${provider.name}. The estimate is a guide.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        }
                    }
                } else if (vm.myServices.isEmpty()) {
                    Text(
                        text = "Add a skill under \"My skills\" tab first, then you can offer it in exchange.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    Text(
                        text = "Select one of your skills to offer:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    sortedMyServices.forEach { s ->
                        val matchesWant = provider.wants.any { w ->
                            s.category.contains(w, ignoreCase = true) || s.subcat.contains(w, ignoreCase = true)
                        }

                        MarketplaceCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = s.id == chosenId,
                                    role = Role.RadioButton,
                                    onClick = { chosenId = s.id }
                                ),
                            border = BorderStroke(
                                1.dp,
                                if (matchesWant) Color(0xFF34D399) else Color(0xFF334155)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = s.id == chosenId,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF34D399), unselectedColor = Color(0xFF94A3B8))
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = s.title.replaceFirstChar { it.uppercase() },
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF8FAFC),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (matchesWant) {
                                            Spacer(Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF065F46))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "⭐ Matches request",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD1FAE5),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${s.subcat} · ${s.category} · ${formatValue(s.valueEur)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }
                        }
                    }

                    if (result != null && mine != null) {
                        FairnessCard(result, provider.name)
                    }
                }
            }
        }

        // Sticky Bottom Action Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    if (!swapMode) {
                        vm.sendPayment(theirs)
                    } else if (mine != null && result != null) {
                        vm.sendExchange(theirs, mine, result)
                    }
                    onDone()
                },
                enabled = !swapMode || (mine != null && result != null),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B))
            ) {
                Text(
                    text = if (!swapMode) "Request Cash Payment" else if (result?.fair == true) "Request Exchange" else "Request Exchange with Balance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF064E3B)
                )
            }
        }
    }
}

@Composable
private fun FairnessCard(r: com.skillexchange.app.ml.ExchangeResult, providerName: String) {
    val containerColor = if (r.fair) Color(0xFF065F46) else Color(0xFF78350F)
    val textColor = if (r.fair) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)

    MarketplaceCard(
        modifier = Modifier.fillMaxWidth(),
        colors = marketplaceCardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (r.fair) {
                Text("Fair Exchange", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                Text(String.format(Locale.US, "The values are within %.0f%% of each other.", r.gapShare * 100), color = Color(0xFFF8FAFC))
            } else {
                Text("Value Gap Identified", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                Text(String.format(Locale.US, "Gap of %s (%.0f%% of the bigger value).", formatKes(r.gap), r.gapShare * 100), color = Color(0xFFF8FAFC))
                if (r.iOfferLess) {
                    Text("Option 1: add a cash top-up of about ${formatKes(r.gap)}.", color = Color(0xFFF8FAFC))
                    if (r.suggestions.isNotEmpty()) Text("Option 2: add an extra service of similar value:", color = Color(0xFFF8FAFC))
                    r.suggestions.forEach {
                        val who = if (it.fromMyServices) "Your skill: " else "e.g. "
                        val matchTag = if (it.matchesWant) " (Matches request ⭐)" else ""
                        Text("• $who${it.label.replaceFirstChar { c -> c.uppercase() }} (${formatKes(it.valueEur)})$matchTag", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD1FAE5))
                    }
                } else {
                    Text("Your skill is worth more. Ask $providerName to add about ${formatKes(r.gap)} in cash or an extra service.", color = Color(0xFFF8FAFC))
                }
            }
            Text("Estimates are a guide based on community benchmarks.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        }
    }
}

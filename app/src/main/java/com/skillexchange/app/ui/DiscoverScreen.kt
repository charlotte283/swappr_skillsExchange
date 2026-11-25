package com.skillexchange.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

import com.skillexchange.app.ml.FuzzySearch

private val DISPLAY_CATEGORIES = listOf(
    "All",
    "Design",
    "Programming",
    "Writing",
    "Photography",
    "Tutoring",
    "Beauty",
    "Home Services",
    "Lifestyle",
    "Music & Audio"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    vm: AppViewModel,
    onOpen: (serviceId: String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddSkillDialog by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    var showPopupMenu by remember { mutableStateOf(false) }

    val all = vm.matches()
    val shown = all.filter { m ->
        val queryMatches = query.isBlank() ||
                FuzzySearch.matches(query, m.service.title) ||
                FuzzySearch.matches(query, m.service.category) ||
                FuzzySearch.matches(query, m.service.subcat) ||
                FuzzySearch.matches(query, m.provider.name)

        val catMatches = if (selectedCategory == "All") true
        else {
            val catLower = selectedCategory.lowercase()
            m.service.category.lowercase().contains(catLower) ||
                    m.service.subcat.lowercase().contains(catLower) ||
                    m.service.title.lowercase().contains(catLower)
        }

        queryMatches && catMatches
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header Section
            item(span = { GridItemSpan(2) }) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Swappr Header Logo & User Avatar Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SwapprHeaderLogo(
                            onNavigateHome = onNavigateHome,
                            subtitle = "Skills Exchange"
                        )

                        // Clickable User Profile Avatar (leads to Profile page)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF065F46))
                                .border(1.dp, Color(0xFF34D399), CircleShape)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (vm.profile.profilePhoto.isNotBlank()) {
                                AsyncImage(
                                    model = vm.profile.profilePhoto,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Personalised Greeting
                    Column(
                        modifier = Modifier.clickable { onNavigateToProfile() }
                    ) {
                        Text(
                            text = "Hello, ${if (vm.profile.name.isBlank()) "Charlotte" else vm.profile.name} 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Find skills to learn & exchange",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Search Bar + Filter Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            placeholder = { Text("Search skills, services...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399)
                                )
                            },
                            colors = marketplaceTextFieldColors(),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        // Filter Button
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF34D399))
                                .clickable { /* filter action */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = Color(0xFF064E3B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Horizontally Scrollable Category Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(DISPLAY_CATEGORIES) { cat ->
                            val isSelected = selectedCategory == cat
                            AssistChip(
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) Color(0xFF34D399) else Color(0xFF1E293B),
                                    labelColor = if (isSelected) Color(0xFF064E3B) else Color(0xFFF8FAFC)
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = if (isSelected) Color(0xFF34D399) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    Text(
                        text = if (selectedCategory == "All") "Popular Services" else "$selectedCategory Services",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Two-Column Grid Service Items
            if (shown.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No services found matching your search.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                items(shown, key = { it.service.id }) { match ->
                    ServiceGridCard(
                        service = match.service,
                        provider = match.provider,
                        isFav = vm.isFavourite(match.service.id),
                        onToggleFav = { vm.toggleFavourite(match.service.id) },
                        onClick = { onOpen(match.service.id) }
                    )
                }
            }
        }

        // Floating Action Button with Hover Prompt Bubble & Pop-up Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 88.dp, end = 16.dp)
        ) {
            // Hover / Prompt Bubble Prompting ("Add Skill")
            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 64.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF34D399)),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = "Add Skill",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier
                            .clickable { showAddSkillDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            ExtendedFloatingActionButton(
                onClick = { showAddSkillDialog = true },
                expanded = isHovered,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Skill"
                    )
                },
                text = {
                    Text(
                        text = "Add Skill",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF064E3B)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFF34D399),
                contentColor = Color(0xFF064E3B),
                modifier = Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Enter -> isHovered = true
                                PointerEventType.Exit -> isHovered = false
                            }
                        }
                    }
                }
            )

            // Pop-up Menu for FAB Action
            DropdownMenu(
                expanded = showPopupMenu,
                onDismissRequest = { showPopupMenu = false },
                modifier = Modifier
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Add Skill", color = Color(0xFFF8FAFC), fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF34D399)) },
                    onClick = {
                        showPopupMenu = false
                        showAddSkillDialog = true
                    }
                )
            }
        }

        if (showAddSkillDialog) {
            AddSkillDialog(
                vm = vm,
                onDismiss = { showAddSkillDialog = false }
            )
        }
    }
}

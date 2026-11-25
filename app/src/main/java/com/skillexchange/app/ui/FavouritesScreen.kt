package com.skillexchange.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FavouritesScreen(
    vm: AppViewModel,
    onOpen: (serviceId: String) -> Unit,
    onNavigateHome: () -> Unit
) {
    val favIds = vm.favouriteIds.toSet()
    val favServices = vm.providerServices.filter { it.id in favIds }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SwapprHeaderLogo(onNavigateHome = onNavigateHome)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Favourites",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Saved services and skills you are interested in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        if (favServices.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No favourites saved yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Tap the heart icon on any service card in Explore to save it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(favServices, key = { it.id }) { service ->
                val provider = vm.providerById(service.ownerId)
                ServiceGridCard(
                    service = service,
                    provider = provider,
                    isFav = true,
                    onToggleFav = { vm.toggleFavourite(service.id) },
                    onClick = { onOpen(service.id) }
                )
            }
        }
    }
}

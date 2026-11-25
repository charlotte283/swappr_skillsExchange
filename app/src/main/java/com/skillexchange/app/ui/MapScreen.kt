package com.skillexchange.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.skillexchange.app.data.AREAS
import com.skillexchange.app.data.Service
import com.skillexchange.app.data.formatValue
import com.skillexchange.app.ml.FuzzySearch
import java.util.Locale

private val MAP_CATEGORIES = listOf("All", "Programming", "Writing", "Lifestyle", "Music & Audio", "Business", "Video & Animation")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    vm: AppViewModel,
    onOpenService: (serviceId: String) -> Unit,
    onNavigateHome: () -> Unit
) {
    // Default center: Nairobi, Kenya (Kilimani area)
    val nairobi = LatLng(-1.2921, 36.7872)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(nairobi, 13f)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var useNativeVectorMap by remember { mutableStateOf(false) } // Default to active Google Maps SDK

    // Filter service listings to display as individual pins on the map
    val filteredServices = vm.providerServices.filter { s ->
        val provider = vm.providerById(s.ownerId)
        val matchesQuery = searchQuery.isBlank() ||
                FuzzySearch.matches(searchQuery, s.title) ||
                FuzzySearch.matches(searchQuery, s.category) ||
                FuzzySearch.matches(searchQuery, s.subcat) ||
                (provider != null && FuzzySearch.matches(searchQuery, provider.name)) ||
                (provider != null && FuzzySearch.matches(searchQuery, provider.area))

        val matchesCategory = if (selectedCategory == "All") true
        else {
            val catLower = selectedCategory.lowercase()
            s.category.lowercase().contains(catLower) || s.subcat.lowercase().contains(catLower)
        }

        matchesQuery && matchesCategory
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        if (useNativeVectorMap) {
            // Interactive Vector Map View
            InteractiveVectorMap(
                services = filteredServices,
                vm = vm,
                onSelectService = { selectedService = it }
            )
        } else {
            // Google Maps SDK View
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                filteredServices.forEach { service ->
                    val provider = vm.providerById(service.ownerId)
                    val areaName = provider?.area ?: "Kilimani"
                    val area = AREAS.firstOrNull { it.name.equals(areaName, ignoreCase = true) } ?: AREAS.first()

                    val jitterLat = (service.id.hashCode() % 100) * 0.00015
                    val jitterLon = (service.id.hashCode() % 97) * 0.00015
                    val markerPosition = LatLng(area.lat + jitterLat, area.lon + jitterLon)

                    Marker(
                        state = MarkerState(position = markerPosition),
                        title = service.title.replaceFirstChar { it.uppercase() },
                        snippet = "${provider?.name ?: "Provider"} · ${formatValue(service.valueEur).split("≈").last().trim()}",
                        onClick = {
                            selectedService = service
                            false
                        },
                        onInfoWindowClick = {
                            onOpenService(service.id)
                        }
                    )
                }
            }
        }

        // Top Overlay Header: Swappr Logo, Search & Category Chips
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row Header
            MarketplaceCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SwapprHeaderLogo(
                        onNavigateHome = onNavigateHome,
                        subtitle = "Service Map"
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF065F46))
                                .clickable { useNativeVectorMap = !useNativeVectorMap }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (useNativeVectorMap) "Vector View (${filteredServices.size})" else "Google Maps (${filteredServices.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD1FAE5)
                            )
                        }
                    }
                }
            }

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = { Text("Search skills or location on map...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF34D399)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                colors = marketplaceTextFieldColors(),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Category Chips Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MAP_CATEGORIES) { cat ->
                    val isSelected = selectedCategory == cat
                    AssistChip(
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
        }

        // Bottom Selected Service Preview Card Overlay
        if (selectedService != null) {
            val service = selectedService!!
            val provider = vm.providerById(service.ownerId)

            MarketplaceCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 90.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Service Image
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
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
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = service.title.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                text = "${provider?.name ?: "Provider"} · ${provider?.area ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formatValue(service.valueEur).split("≈").last().trim(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )

                            Button(
                                onClick = { onOpenService(service.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B)),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("View & Swap", style = MaterialTheme.typography.labelMedium, color = Color(0xFF064E3B))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Vector Map Component that renders Nairobi roads, neighborhood boundaries & service markers dynamically */
@Composable
private fun InteractiveVectorMap(
    services: List<Service>,
    vm: AppViewModel,
    onSelectService: (Service) -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1.0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomFactor, _ ->
                    offset += pan
                    zoom = (zoom * zoomFactor).coerceIn(0.6f, 3.0f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f) + offset

            // Draw grid roads
            val roadPath = Path()
            // Main arterial roads
            roadPath.moveTo(center.x - 400 * zoom, center.y - 200 * zoom)
            roadPath.lineTo(center.x + 400 * zoom, center.y + 300 * zoom)
            roadPath.moveTo(center.x - 300 * zoom, center.y + 400 * zoom)
            roadPath.lineTo(center.x + 500 * zoom, center.y - 300 * zoom)

            drawPath(
                path = roadPath,
                color = Color(0xFF1E293B),
                style = Stroke(width = 12f * zoom)
            )

            // Secondary roads
            for (i in -3..3) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    start = Offset(center.x + (i * 180) * zoom, 0f),
                    end = Offset(center.x + (i * 180) * zoom, size.height),
                    strokeWidth = 4f * zoom
                )
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    start = Offset(0f, center.y + (i * 180) * zoom),
                    end = Offset(size.width, center.y + (i * 180) * zoom),
                    strokeWidth = 4f * zoom
                )
            }
        }

        // Render Service Pins on top of Vector Map
        services.forEachIndexed { index, service ->
            val provider = vm.providerById(service.ownerId)
            val areaName = provider?.area ?: "Kilimani"
            val areaIndex = AREAS.indexOfFirst { it.name.equals(areaName, ignoreCase = true) }.coerceAtLeast(0)

            // Positional calculation for vector view
            val angle = (areaIndex * 45) * (Math.PI / 180.0)
            val radius = 180.0 + (index * 25)
            val posX = 500f + (radius * Math.cos(angle)).toFloat()
            val posY = 750f + (radius * Math.sin(angle)).toFloat()

            Box(
                modifier = Modifier
                    .padding(start = posX.dp, top = posY.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF34D399), RoundedCornerShape(16.dp))
                    .clickable { onSelectService(service) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = service.title.take(18) + "...",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                }
            }
        }
    }
}

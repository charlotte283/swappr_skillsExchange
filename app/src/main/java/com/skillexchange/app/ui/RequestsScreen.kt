package com.skillexchange.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skillexchange.app.data.RequestStatus
import com.skillexchange.app.data.RequestType
import com.skillexchange.app.data.ServiceRequest
import com.skillexchange.app.data.formatKes

@Composable
fun RequestsScreen(
    vm: AppViewModel,
    onNavigateHome: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SwapprHeaderLogo(onNavigateHome = onNavigateHome)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "My Exchanges",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Track active skill swaps, cash requests, and provider ratings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
        if (vm.requests.isEmpty()) {
            item {
                Text(
                    text = "No exchange requests yet. Explore skills in the Explore tab!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        }
        items(vm.requests, key = { it.id }) { r -> RequestCard(r, vm) }
    }
}

@Composable
private fun RequestCard(r: ServiceRequest, vm: AppViewModel) {
    MarketplaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = r.theirTitle.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.weight(1f)
                )

                val statusColor = when (r.status) {
                    RequestStatus.PENDING -> Color(0xFFFBBF24)
                    RequestStatus.ACCEPTED -> Color(0xFF34D399)
                    RequestStatus.COMPLETED -> Color(0xFF60A5FA)
                }

                Text(
                    text = r.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier
                        .padding(start = 8.dp)
                )
            }

            Text(
                text = "Provider: ${r.providerName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )

            if (r.type == RequestType.EXCHANGE) {
                Text("You offer: ${r.myTitle.orEmpty().replaceFirstChar { it.uppercase() }}", color = Color(0xFFF8FAFC))
                if (r.topUpEur > 0) {
                    Text("Cash Balance Top-Up: ${formatKes(r.topUpEur)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                }
            } else {
                Text("Cash Payment: ${formatKes(r.theirValueEur)}", color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
            }

            when (r.status) {
                RequestStatus.PENDING -> Button(
                    onClick = { vm.advance(r.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B))
                ) { Text("Demo: Provider Accepts Swap", color = Color(0xFF064E3B)) }
                RequestStatus.ACCEPTED -> Button(
                    onClick = { vm.advance(r.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color(0xFF064E3B))
                ) { Text("Mark Exchange Completed", color = Color(0xFF064E3B)) }
                RequestStatus.COMPLETED -> {
                    if (r.myRating == null) {
                        Text("Rate ${r.providerName}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { vm.rate(r.id, star) }) {
                                    Icon(Icons.Outlined.StarBorder, contentDescription = "$star stars", tint = Color(0xFFFBBF24))
                                }
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Your Rating: ", fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                            repeat(r.myRating) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFBBF24)) }
                        }
                    }
                }
            }
        }
    }
}

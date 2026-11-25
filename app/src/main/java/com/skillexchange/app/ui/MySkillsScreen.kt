package com.skillexchange.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skillexchange.app.data.formatValue

@Composable
fun MySkillsScreen(
    vm: AppViewModel,
    onNavigateHome: () -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SwapprHeaderLogo(onNavigateHome = onNavigateHome)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Skills I Offer",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "List your skills for exchange. Valued by community benchmark model.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
            if (vm.myServices.isEmpty()) {
                item {
                    Text(
                        text = "Nothing here yet. Tap + below to list your first skill.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            items(vm.myServices, key = { it.id }) { s ->
                MarketplaceCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = s.title.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                            Text(
                                text = "${s.subcat} · ${s.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = formatValue(s.valueEur),
                                color = Color(0xFF34D399),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { vm.removeService(s.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remove skill",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF34D399),
            contentColor = Color(0xFF064E3B)
        ) { Icon(Icons.Filled.Add, contentDescription = "Add skill") }
    }

    if (showAdd) AddSkillDialog(vm, onDismiss = { showAdd = false })
}

@Composable
fun AddSkillDialog(vm: AppViewModel, onDismiss: () -> Unit) {
    val taxonomy = vm.estimator.taxonomy
    val categories = taxonomy.keys.toList()
    val levels = listOf("Beginner", "Intermediate", "Advanced", "Expert")
    val hoursList = listOf(1, 3, 5, 10, 20, 40)

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var subcat by remember { mutableStateOf(taxonomy.getValue(categories.first()).first()) }
    var level by remember { mutableStateOf("Intermediate") }
    var hours by remember { mutableStateOf(5) }

    val subcats = taxonomy[category].orEmpty()
    val value = vm.previewValue(title, category, subcat, level, hours)
    val factors = vm.explain(title, category, subcat)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1E293B),
        title = { Text("List a New Skill", fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text("What skill will you offer?") },
                    placeholder = { Text("e.g. design a modern logo") },
                    colors = marketplaceTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Dropdown("Category", categories, category, {
                    category = it
                    subcat = taxonomy[it].orEmpty().firstOrNull().orEmpty()
                })
                Dropdown("Sub-category", subcats, subcat, { subcat = it })
                Dropdown("Experience Level", levels, level, { level = it })
                Dropdown("Estimated Effort", hoursList.map { "$it hrs" }, "$hours hrs", {
                    hours = it.replace(" hrs", "").toIntOrNull() ?: 5
                })

                ValueBlock(value, factors, vm.estimator.isRidge)
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { vm.addService(title, category, subcat, level, hours); onDismiss() }
            ) { Text("Add Skill", fontWeight = FontWeight.Bold, color = Color(0xFF34D399)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF94A3B8)) }
        },
    )
}

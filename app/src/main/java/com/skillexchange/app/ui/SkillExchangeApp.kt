package com.skillexchange.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("discover", "Explore", Icons.Filled.Home),
    Tab("map", "Map", Icons.Filled.Map),
    Tab("requests", "Exchanges", Icons.Filled.SwapHoriz),
    Tab("favourites", "Favourites", Icons.Filled.Favorite),
    Tab("profile", "Profile", Icons.Filled.Person),
)

@Composable
fun SkillExchangeApp(vm: AppViewModel = viewModel()) {
    if (!vm.isLoggedIn) {
        AuthScreen(
            onLogin = { email, pass -> vm.login(email, pass) },
            onRegister = { email, pass, name, area, bio -> vm.register(email, pass, name, area, bio) }
        )
        return
    }

    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    val onNavigateHome: () -> Unit = {
        nav.navigate("discover") {
            popUpTo("discover") { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        bottomBar = {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(12.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(32.dp))
            ) {
                NavigationBar(
                    containerColor = Color(0xFF1E293B),
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    tabs.forEach { tab ->
                        val selected = route == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (tab.route == "discover") {
                                    onNavigateHome()
                                } else {
                                    nav.navigate(tab.route) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF34D399),
                                selectedTextColor = Color(0xFF34D399),
                                indicatorColor = Color(0xFF065F46),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = "discover", modifier = Modifier.padding(padding)) {
            composable("discover") {
                DiscoverScreen(
                    vm = vm,
                    onOpen = { id -> nav.navigate("exchange/$id") },
                    onNavigateHome = onNavigateHome,
                    onNavigateToProfile = { nav.navigate("profile") }
                )
            }
            composable("map") {
                MapScreen(
                    vm = vm,
                    onOpenService = { id -> nav.navigate("exchange/$id") },
                    onNavigateHome = onNavigateHome
                )
            }
            composable("requests") {
                RequestsScreen(
                    vm = vm,
                    onNavigateHome = onNavigateHome
                )
            }
            composable("favourites") {
                FavouritesScreen(
                    vm = vm,
                    onOpen = { id -> nav.navigate("exchange/$id") },
                    onNavigateHome = onNavigateHome
                )
            }
            composable("skills") {
                MySkillsScreen(
                    vm = vm,
                    onNavigateHome = onNavigateHome
                )
            }
            composable("profile") {
                ProfileScreen(
                    vm = vm,
                    onNavigateHome = onNavigateHome,
                    onNavigateToMySkills = { nav.navigate("skills") }
                )
            }
            composable("exchange/{id}") { back ->
                ExchangeScreen(
                    vm = vm,
                    serviceId = back.arguments?.getString("id").orEmpty(),
                    onDone = { nav.navigate("requests") { popUpTo("discover") } },
                    onBack = { nav.popBackStack() },
                    onNavigateHome = onNavigateHome
                )
            }
        }
    }
}

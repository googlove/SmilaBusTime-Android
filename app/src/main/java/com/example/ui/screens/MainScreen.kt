package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.models.BusRoute
import com.example.data.repository.BusRepository

enum class MainTab {
    ROUTES, PLANNER, INFO
}

@Composable
fun MainScreen(
    repository: BusRepository,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(MainTab.ROUTES) }
    var selectedBus by remember { mutableStateOf<BusRoute?>(null) }

    if (selectedBus != null) {
        RouteDetailScreen(
            bus = selectedBus!!,
            repository = repository,
            onBack = { selectedBus = null },
            modifier = modifier
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.ROUTES,
                        onClick = { currentTab = MainTab.ROUTES },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.ROUTES) Icons.Filled.DirectionsBus else Icons.Outlined.DirectionsBus,
                                contentDescription = "Маршрути"
                            )
                        },
                        label = { Text("Маршрути") },
                        modifier = Modifier.testTag("tab_routes")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.PLANNER,
                        onClick = { currentTab = MainTab.PLANNER },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.PLANNER) Icons.Filled.Explore else Icons.Outlined.Explore,
                                contentDescription = "Планувальник"
                            )
                        },
                        label = { Text("Планувальник") },
                        modifier = Modifier.testTag("tab_planner")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.INFO,
                        onClick = { currentTab = MainTab.INFO },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.INFO) Icons.Filled.Info else Icons.Outlined.Info,
                                contentDescription = "Інформація"
                            )
                        },
                        label = { Text("Інформація") },
                        modifier = Modifier.testTag("tab_info")
                    )
                }
            },
            modifier = modifier
        ) { innerPadding ->
            when (currentTab) {
                MainTab.ROUTES -> {
                    RoutesScreen(
                        repository = repository,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onSelectBus = { selectedBus = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                MainTab.PLANNER -> {
                    TripPlannerScreen(
                        repository = repository,
                        onSelectBus = { selectedBus = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                MainTab.INFO -> {
                    InfoScreen(
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

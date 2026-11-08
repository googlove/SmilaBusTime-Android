package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BusRoute
import com.example.data.repository.BusRepository
import com.example.ui.components.BusCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BusBluePrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class RouteFilter {
    ALL, URBAN, SUBURBAN, FAVORITES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    repository: BusRepository,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSelectBus: (BusRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(RouteFilter.ALL) }
    var favorites by remember { mutableStateOf(repository.getFavorites()) }
    var currentTimeStr by remember { mutableStateOf("") }

    // Update live clock every second
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTimeStr = sdf.format(Date())
            delay(1000)
        }
    }

    val allRoutes = remember { repository.getBusRoutes() }

    val filteredRoutes = remember(searchQuery, selectedFilter, favorites, allRoutes) {
        val q = searchQuery.trim().lowercase()
        val numQuery = q.removePrefix("№").removePrefix("#").trim()
        allRoutes.filter { bus ->
            // Filter by type / favorites
            val matchesFilter = when (selectedFilter) {
                RouteFilter.ALL -> true
                RouteFilter.URBAN -> bus.isUrban
                RouteFilter.SUBURBAN -> !bus.isUrban
                RouteFilter.FAVORITES -> favorites.contains(bus.number)
            }

            // Filter by search query (number, destination/title, direction name, stops)
            val matchesSearch = if (q.isEmpty()) true else {
                bus.number.lowercase() == numQuery ||
                bus.number.lowercase().contains(numQuery) ||
                bus.title.lowercase().contains(q) ||
                bus.routes.any { r ->
                    r.direction.lowercase().contains(q) ||
                    r.stops.any { it.name.lowercase().contains(q) }
                } ||
                (bus.directionRoutes?.forward?.let { f ->
                    f.name.lowercase().contains(q) || f.stops.any { it.name.lowercase().contains(q) }
                } == true) ||
                (bus.directionRoutes?.backward?.let { b ->
                    b.name.lowercase().contains(q) || b.stops.any { it.name.lowercase().contains(q) }
                } == true)
            }

            matchesFilter && matchesSearch
        }.sortedWith { a, b ->
            val aFav = favorites.contains(a.number)
            val bFav = favorites.contains(b.number)
            if (aFav && !bFav) -1
            else if (!aFav && bFav) 1
            else {
                val aNum = a.number.toIntOrNull()
                val bNum = b.number.toIntOrNull()
                if (aNum != null && bNum != null) aNum.compareTo(bNum)
                else a.number.compareTo(b.number)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo and Title
                    Text(
                        text = "🚌 SmilaBusTime",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = BusBluePrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Clock Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = currentTimeStr,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Telegram Bot Button
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/SmilaBusTime_bot"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Telegram Бот",
                            tint = Color(0xFF24A1DE),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Змінити тему",
                            tint = if (isDarkTheme) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar at Top of Main Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar")
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Пошук за номером (напр. 3 або 302) чи напрямком…",
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Пошук",
                                tint = if (searchQuery.isNotEmpty()) BusBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.testTag("search_clear_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Очистити пошук",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = BusBluePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )

                    // Active Search feedback row
                    if (searchQuery.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Знайдено: ${filteredRoutes.size} маршрут${if (filteredRoutes.size in 2..4) "и" else if (filteredRoutes.size == 1) "" else "ів"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BusBluePrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Скинути",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { searchQuery = "" }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        // Quick destination & number search chips
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickFilters = listOf("№3", "№302", "№4", "№30", "Волошкова", "Черкаси", "АС-2", "Центр")
                            items(quickFilters) { item ->
                                SuggestionChip(
                                    onClick = { searchQuery = item },
                                    label = { Text(item, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == RouteFilter.ALL,
                        onClick = { selectedFilter = RouteFilter.ALL },
                        label = { Text("Усі (${allRoutes.size})") },
                        modifier = Modifier.testTag("filter_all")
                    )
                    FilterChip(
                        selected = selectedFilter == RouteFilter.URBAN,
                        onClick = { selectedFilter = RouteFilter.URBAN },
                        label = { Text("Міські") },
                        modifier = Modifier.testTag("filter_urban")
                    )
                    FilterChip(
                        selected = selectedFilter == RouteFilter.SUBURBAN,
                        onClick = { selectedFilter = RouteFilter.SUBURBAN },
                        label = { Text("Приміські") },
                        modifier = Modifier.testTag("filter_suburban")
                    )
                    FilterChip(
                        selected = selectedFilter == RouteFilter.FAVORITES,
                        onClick = { selectedFilter = RouteFilter.FAVORITES },
                        label = { Text("⭐ (${favorites.size})") },
                        modifier = Modifier.testTag("filter_favorites")
                    )
                }
            }
        }

        // Support Project Mini Banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://googlove.netlify.app/donate/index.html"))
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Підтримати розвиток розкладу Сміли",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "💸 Донат",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BusBluePrimary
                )
            }
        }

        // List of routes
        if (filteredRoutes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔍",
                        fontSize = 44.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Нічого не знайдено",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Спробуйте змінити фільтр або пошуковий запит",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("routes_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRoutes, key = { it.number }) { bus ->
                    val nextDep = remember(bus, currentTimeStr) {
                        repository.findNextDeparture(bus)
                    }
                    val isFav = favorites.contains(bus.number)

                    BusCard(
                        bus = bus,
                        nextDeparture = nextDep,
                        isFavorite = isFav,
                        onToggleFavorite = {
                            val newFav = repository.toggleFavorite(bus.number)
                            favorites = repository.getFavorites()
                        },
                        onClick = { onSelectBus(bus) }
                    )
                }
            }
        }
    }
}

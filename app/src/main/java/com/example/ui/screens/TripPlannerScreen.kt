package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BusRoute
import com.example.data.models.TripPlanResult
import com.example.data.repository.BusRepository
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BusBluePrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripPlannerScreen(
    repository: BusRepository,
    onSelectBus: (BusRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    var fromStop by remember { mutableStateOf("") }
    var toStop by remember { mutableStateOf("") }
    var isFromFocused by remember { mutableStateOf(false) }
    var isToFocused by remember { mutableStateOf(false) }

    val allStops = remember { repository.getAllStopNames() }
    val popularStops = remember {
        listOf("АС-2", "Центр", "Школа №3", "Пушка", "Вул. Волошкова", "Юрова гора", "Завод СМЕЗ", "Вокзал")
    }

    val results = remember(fromStop, toStop) {
        if (fromStop.isNotBlank() && toStop.isNotBlank() && fromStop.trim() != toStop.trim()) {
            repository.planTrip(fromStop, toStop)
        } else {
            emptyList()
        }
    }

    val fromSuggestions = remember(fromStop, allStops) {
        if (fromStop.length < 2) emptyList()
        else allStops.filter { it.contains(fromStop.trim(), ignoreCase = true) }.take(5)
    }

    val toSuggestions = remember(toStop, allStops) {
        if (toStop.length < 2) emptyList()
        else allStops.filter { it.contains(toStop.trim(), ignoreCase = true) }.take(5)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Planner Header & Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BusBluePrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = BusBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Прокласти маршрут",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Оберіть зупинки — покажу, коли і чим їхати",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // From input
                    OutlinedTextField(
                        value = fromStop,
                        onValueChange = { fromStop = it },
                        label = { Text("Звідки (Початкова зупинка)") },
                        leadingIcon = {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        },
                        trailingIcon = {
                            if (fromStop.isNotEmpty()) {
                                IconButton(onClick = { fromStop = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистити")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tp_from_input")
                    )

                    // From suggestions
                    if (fromSuggestions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column {
                                fromSuggestions.forEach { s ->
                                    Text(
                                        text = s,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { fromStop = s }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Swap button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                val temp = fromStop
                                fromStop = toStop
                                toStop = temp
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("tp_swap_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Поміняти зупинки місцями",
                                tint = BusBluePrimary
                            )
                        }
                    }

                    // To input
                    OutlinedTextField(
                        value = toStop,
                        onValueChange = { toStop = it },
                        label = { Text("Куди (Кінцева зупинка)") },
                        leadingIcon = {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("B", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        },
                        trailingIcon = {
                            if (toStop.isNotEmpty()) {
                                IconButton(onClick = { toStop = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистити")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tp_to_input")
                    )

                    // To suggestions
                    if (toSuggestions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column {
                                toSuggestions.forEach { s ->
                                    Text(
                                        text = s,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { toStop = s }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Popular stops chips
                    Text(
                        text = "Популярні зупинки:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(popularStops) { stop ->
                            SuggestionChip(
                                onClick = {
                                    if (fromStop.isEmpty()) fromStop = stop
                                    else if (toStop.isEmpty() && fromStop != stop) toStop = stop
                                    else fromStop = stop
                                },
                                label = { Text(stop, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Search Results
        if (fromStop.isNotBlank() && toStop.isNotBlank()) {
            if (results.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🤔", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Прямого рейсу не знайдено",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Схоже, між «$fromStop» та «$toStop» немає прямого маршруту. Спробуйте сусідню зупинку або пересадку в центрі міста.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Header of results
                item {
                    val nextUpcoming = results.firstOrNull { it.today && it.diff >= 0 }
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (nextUpcoming != null) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (nextUpcoming != null) {
                                        "🟢 Найближчий рейс — №${nextUpcoming.busNumber} о ${repository.minToHHMM(nextUpcoming.boardMin)} (${repository.formatDiffMinutes(nextUpcoming.diff)})"
                                    } else {
                                        "🌙 На сьогодні рейсів більше немає — показано повний розклад"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (nextUpcoming != null) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Result Cards
                items(results) { res ->
                    val busColor = parseHexColor(res.color)
                    val isPast = res.diff < 0
                    val isUpcomingSoon = res.today && res.diff in 0..15

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBus(res.bus) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = busColor,
                                    modifier = Modifier.padding(end = 10.dp)
                                ) {
                                    Text(
                                        text = "№${res.busNumber}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = res.dirName.ifEmpty { res.busTitle },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (res.urban) "🏙️ Міський" else "🌄 Приміський",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Countdown pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        !res.today -> MaterialTheme.colorScheme.surfaceVariant
                                        isUpcomingSoon -> Color(0xFFDCFCE7)
                                        isPast -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            !res.today -> "не сьогодні"
                                            isPast -> repository.minToHHMM(res.boardMin)
                                            else -> repository.formatDiffMinutes(res.diff)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = when {
                                            !res.today -> MaterialTheme.colorScheme.onSurfaceVariant
                                            isUpcomingSoon -> Color(0xFF166534)
                                            isPast -> MaterialTheme.colorScheme.onSurfaceVariant
                                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Timeline
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = repository.minToHHMM(res.boardMin),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = res.fromName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (res.rideMin != null) "🕒 ≈${repository.formatRide(res.rideMin)}" else "→",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BusBluePrimary
                                    )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 1.dp,
                                        modifier = Modifier.width(60.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (res.arriveMin != null) repository.minToHHMM(res.arriveMin) else "--:--",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = res.toName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!res.note.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "📅 ${res.note}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (res.today) MaterialTheme.colorScheme.primary else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

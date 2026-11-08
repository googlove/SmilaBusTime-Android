package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BusRoute
import com.example.data.repository.BusRepository
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BusBluePrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RouteDetailScreen(
    bus: BusRoute,
    repository: BusRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(repository.isFavorite(bus.number)) }
    var selectedDirectionIndex by remember { mutableIntStateOf(0) }
    var reminderTimeDialog by remember { mutableStateOf<String?>(null) }
    val routeColor = parseHexColor(bus.color)
    val curMinutes = repository.getCurrentMinutes()

    // Determine direction tabs
    val hasDirectionRoutes = bus.directionRoutes != null &&
            (bus.directionRoutes.forward != null || bus.directionRoutes.backward != null)

    val currentDirData = if (hasDirectionRoutes) {
        if (selectedDirectionIndex == 0) bus.directionRoutes?.forward else bus.directionRoutes?.backward
    } else null

    val currentFallbackRoute = if (!hasDirectionRoutes && bus.routes.isNotEmpty()) {
        bus.routes.getOrNull(selectedDirectionIndex) ?: bus.routes.first()
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = routeColor,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "№${bus.number}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = bus.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    // Google Maps link
                    if (!bus.mapIframeSrc.isNullOrBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bus.mapIframeSrc))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("detail_map_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Відкрити карту маршруту",
                                tint = BusBluePrimary
                            )
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = {
                            isFavorite = repository.toggleFavorite(bus.number)
                        },
                        modifier = Modifier.testTag("detail_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "В обраному" else "Додати в обране",
                            tint = if (isFavorite) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Direction selector tabs
            if (hasDirectionRoutes) {
                item {
                    TabRow(
                        selectedTabIndex = selectedDirectionIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedDirectionIndex == 0,
                            onClick = { selectedDirectionIndex = 0 },
                            text = { Text("Прямий", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedDirectionIndex == 1,
                            onClick = { selectedDirectionIndex = 1 },
                            text = { Text("Зворотній", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            } else if (bus.routes.size > 1) {
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedDirectionIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        bus.routes.forEachIndexed { index, route ->
                            Tab(
                                selected = selectedDirectionIndex == index,
                                onClick = { selectedDirectionIndex = index },
                                text = { Text(route.direction.take(22) + if (route.direction.length > 22) "…" else "") }
                            )
                        }
                    }
                }
            }

            // Direction info banner
            item {
                val dirTitle = currentDirData?.name ?: currentFallbackRoute?.direction ?: bus.title
                val workDays = currentDirData?.workDays ?: currentFallbackRoute?.workDays

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = routeColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dirTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!workDays.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = workDays,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Stops list with departures
            if (currentDirData != null) {
                // RENDER FROM DIRECTION DATA WITH OFFSETS
                val departureTimes = currentDirData.departureTimes

                items(currentDirData.stops) { stop ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(routeColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stop.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (stop.offsetMin > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${stop.offsetMin} хв",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Flow row of times calculated with offset
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                departureTimes.forEach { depStr ->
                                    val baseMin = repository.parseTimeStr(depStr) ?: return@forEach
                                    val stopMin = baseMin + stop.offsetMin
                                    val note = repository.extractDayNote(depStr)
                                    val isUpcoming = stopMin >= curMinutes && repository.noteMatchesToday(note)
                                    val timeFormatted = repository.minToHHMM(stopMin)

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isUpcoming) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                reminderTimeDialog = "$timeFormatted на зупинці «${stop.name}»"
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = timeFormatted,
                                                fontWeight = if (isUpcoming) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp,
                                                color = if (isUpcoming) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (!note.isNullOrBlank()) {
                                                Text(
                                                    text = " ($note)",
                                                    fontSize = 11.sp,
                                                    color = if (isUpcoming) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (currentFallbackRoute != null) {
                // RENDER FROM ROUTE STOPS LIST
                items(currentFallbackRoute.stops) { stop ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(routeColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stop.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                stop.times.forEach { timeStr ->
                                    val tMin = repository.parseTimeStr(timeStr) ?: return@forEach
                                    val note = repository.extractDayNote(timeStr)
                                    val isUpcoming = tMin >= curMinutes && repository.noteMatchesToday(note)
                                    val timeOnly = timeStr.split(" ")[0]

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isUpcoming) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                reminderTimeDialog = "$timeOnly на зупинці «${stop.name}»"
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = timeOnly,
                                                fontWeight = if (isUpcoming) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp,
                                                color = if (isUpcoming) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (!note.isNullOrBlank()) {
                                                Text(
                                                    text = " ($note)",
                                                    fontSize = 11.sp,
                                                    color = if (isUpcoming) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fare and Note card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "💰 Вартість проїзду: ${bus.fare}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (!bus.fareNote.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bus.fareNote.replace("<br>", "\n"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Reminder Dialog
    if (reminderTimeDialog != null) {
        val target = reminderTimeDialog!!
        AlertDialog(
            onDismissRequest = { reminderTimeDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = AccentGold
                )
            },
            title = { Text("Нагадування про рейс") },
            text = {
                Text("Бажаєте встановити нагадування на $target?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(100)
                            }
                        } catch (_: Exception) {}

                        Toast.makeText(context, "⏰ Нагадування збережено на $target", Toast.LENGTH_SHORT).show()
                        reminderTimeDialog = null
                    }
                ) {
                    Text("Встановити")
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderTimeDialog = null }) {
                    Text("Скасувати")
                }
            }
        )
    }
}

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.BusRepository
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BusBluePrimary

@Composable
fun InfoScreen(
    repository: BusRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val info = remember { repository.getAppInfo() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚌 SmilaBusTime",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = BusBluePrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Розклад міських та приміських автобусів м. Сміла",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "🗓️ Оновлено: ${info.updateDate}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Quick Links Section
        item {
            Text(
                text = "Зв'язок та підтримка",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Telegram Bot
                    ListItem(
                        headlineContent = { Text("Telegram-бот розкладу", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("@SmilaBusTime_bot") },
                        leadingContent = {
                            Surface(shape = CircleShape, color = Color(0xFF24A1DE).copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF24A1DE), modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/SmilaBusTime_bot")))
                            }
                            .testTag("info_bot_link")
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Feedback / Developer
                    ListItem(
                        headlineContent = { Text("Зворотний зв'язок", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("@flame4ost у Telegram") },
                        leadingContent = {
                            Surface(shape = CircleShape, color = BusBluePrimary.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = BusBluePrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/flame4ost")))
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Donate
                    ListItem(
                        headlineContent = { Text("Підтримати проєкт", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Допомога серверу та оновленням") },
                        leadingContent = {
                            Surface(shape = CircleShape, color = AccentGold.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://googlove.netlify.app/donate/index.html")))
                        }
                    )
                }
            }
        }

        // Fares Section
        item {
            Text(
                text = "💰 Вартість проїзду",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(info.generalInfo.busFares) { fareItem ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = fareItem.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = fareItem.text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Privileges Section
        item {
            Text(
                text = "🛡️ Пільги на безкоштовний проїзд",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(info.generalInfo.privileges) { privItem ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = privItem.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = privItem.text,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Carpool Section (Попутка)
        item {
            Text(
                text = "🚗 Попутка Сміла — Черкаси",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Середня ціна: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = info.poputka.price,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    info.poputka.routes.forEach { r ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📍 ${r.city}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BusBluePrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Початкова: ${r.start}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "Кінцева: ${r.end}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Спільноти попуток:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    info.poputka.links.forEach { link ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (link.iconType == "viber") Color(0xFF7360F2).copy(alpha = 0.15f) else Color(0xFF24A1DE).copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (link.iconType == "viber") "💜 Viber" else "✈️ Telegram",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (link.iconType == "viber") Color(0xFF7360F2) else Color(0xFF24A1DE)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = link.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // About Project & System
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Про систему розкладу",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SmilaBusTime містить офіційний та перевірений розклад автобусних маршрутів Сміли. Графіки руху оновлюються за даними міськради та перевізників. Сервіс працює повністю автономно й офлайн.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "© 2022–2026 SmilaBusTime | Розроблено для мешканців та гостей Сміли",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

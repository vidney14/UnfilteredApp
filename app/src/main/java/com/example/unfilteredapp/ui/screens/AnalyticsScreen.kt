package com.example.unfilteredapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unfilteredapp.data.model.MoodCount
import com.example.unfilteredapp.data.model.MoodLogEntry
import com.example.unfilteredapp.ui.theme.SanctuaryDesign
import com.example.unfilteredapp.ui.theme.moodAccentColor
import com.example.unfilteredapp.viewmodel.AnalyticsState
import com.example.unfilteredapp.viewmodel.MoodAnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MoodAnalyticsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.analyticsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAnalytics(7)
    }

    SanctuaryDesign.GlassyBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SanctuaryDesign.SanctuaryTopBar(
                    title = "Insights",
                    subtitle = "Your emotional patterns",
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNavigationClick = onBack,
                    actions = {
                        IconButton(onClick = { viewModel.fetchAnalytics(7) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (val currentState = state) {
                    is AnalyticsState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is AnalyticsState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentState.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SanctuaryDesign.PrimaryButton(text = "Try Again", onClick = { viewModel.fetchAnalytics(7) })
                        }
                    }
                    is AnalyticsState.Success -> {
                        AnalyticsContent(
                            totalLogs = currentState.data.totalLogs,
                            counts = currentState.data.moodCounts, 
                            logs = currentState.data.dailyLogs
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun AnalyticsContent(totalLogs: Int, counts: List<MoodCount>, logs: List<MoodLogEntry>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp)
    ) {
        item {
            TotalLogsCard(totalLogs)
        }

        item {
            Column {
                Text(
                    "Energy Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Breakdown of your moods in the last 7 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            MoodDistributionChart(counts)
        }

        if (counts.isNotEmpty()) {
            item {
                SectionHeader("Key Observations", Icons.Default.TrendingUp)
            }

            item {
                val primaryMood = counts.maxByOrNull { it.count }
                InsightCard(
                    title = "Dominant Energy",
                    description = "You've spent most of your time in a '${primaryMood?.modeType?.replace("_", " ")}' state.",
                    accentColor = moodAccentColor(primaryMood?.modeType ?: "")
                )
            }
        }

        item {
            SectionHeader("History Feed", Icons.Default.Analytics)
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    "No logs found. Start tracking to see your history!",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(logs) { log ->
                RecentLogItem(log)
            }
        }
    }
}

@Composable
fun TotalLogsCard(count: Int) {
    SanctuaryDesign.SanctuaryCard(
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    "Check-ins",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "$count",
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
            }
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp)
            )
        }
    }
}

@Composable
fun MoodDistributionChart(counts: List<MoodCount>) {
    val total = counts.sumOf { it.count }.toFloat()

    SanctuaryDesign.SanctuaryCard {
        if (counts.isEmpty()) {
            Text(
                "Start logging to build your profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            counts.forEachIndexed { index, item ->
                val targetProgress = if (total > 0) item.count / total else 0f
                val color = moodAccentColor(item.modeType)

                // Animate progress bar fill on first composition
                val animatedProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(
                        durationMillis = 900,
                        delayMillis = index * 120,
                        easing = FastOutSlowInEasing
                    ),
                    label = "progress_${item.modeType}"
                )

                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.modeType.replace("_", " ").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun InsightCard(title: String, description: String, accentColor: Color) {
    SanctuaryDesign.SanctuaryCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp, 40.dp).clip(CircleShape).background(accentColor))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RecentLogItem(log: MoodLogEntry) {
    val accentColor = moodAccentColor(log.modeType)

    SanctuaryDesign.SanctuaryCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        log.modeSubType.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.modeSubType, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text(
                    log.modeType.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = log.date.split("T").firstOrNull() ?: log.date,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

package com.example.unfilteredapp.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unfilteredapp.ui.theme.SanctuaryDesign
import com.example.unfilteredapp.viewmodel.JournalState
import com.example.unfilteredapp.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(onBack: () -> Unit, viewModel: JournalViewModel) {
    var textState by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    val journalState by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showHistory by remember { mutableStateOf(false) }
    
    val currentDate = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()) }

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isRecording = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (spokenText != null) {
                textState = if (textState.isBlank()) spokenText else "$textState $spokenText"
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchEntries()
    }

    SanctuaryDesign.GlassyBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SanctuaryDesign.SanctuaryTopBar(
                    title = "Reflection",
                    subtitle = currentDate,
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNavigationClick = onBack,
                    actions = {
                        IconButton(
                            onClick = { showHistory = true },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRecording) {
                            Surface(
                                modifier = Modifier.size(64.dp).scale(pulseScale),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {}
                        }
                        FloatingActionButton(
                            onClick = {
                                isRecording = true
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Holding space for your thoughts...")
                                }
                                speechLauncher.launch(intent)
                            },
                            containerColor = if (isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isRecording) Color.White else MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Record")
                        }
                    }
                    
                    if (textState.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ExtendedFloatingActionButton(
                            onClick = {
                                viewModel.addEntry(textState)
                                textState = ""
                            },
                            icon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
                            text = { Text("Preserve", fontWeight = FontWeight.Black) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
                        placeholder = { 
                            Text(
                                "What's the unfiltered truth today?",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-1).sp
                                )
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    // Word count indicator (bottom-right, subtle)
                    if (textState.isNotBlank()) {
                        val wordCount = textState.trim().split(Regex("\\s+")).size
                        Text(
                            text = "$wordCount ${if (wordCount == 1) "word" else "words"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = androidx.compose.ui.Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 12.dp, end = 24.dp)
                        )
                    }
                }
            }

            if (showHistory) {
                ModalBottomSheet(
                    onDismissRequest = { showHistory = false },
                    sheetState = sheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    HistorySheetContent(journalState)
                }
            }
        }
    }
}

@Composable
fun HistorySheetContent(state: JournalState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "My Journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
        }
        
        when (state) {
            is JournalState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                CircularProgressIndicator(strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary) 
            }
            is JournalState.Success -> {
                if (state.entries.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Your paper is waiting for its first story.", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 40.dp, top = 12.dp)
                    ) {
                        itemsIndexed(state.entries) { index, entry ->
                            HistoryItem(entry.content, entry.created_at)
                        }
                    }
                }
            }
            is JournalState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}

@Composable
fun HistoryItem(content: String, timestamp: String) {
    SanctuaryDesign.SanctuaryCard {
        Column {
            Text(
                text = timestamp.split("T").firstOrNull() ?: timestamp,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

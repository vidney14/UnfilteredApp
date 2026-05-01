package com.example.unfilteredapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unfilteredapp.data.model.MoodData
import com.example.unfilteredapp.data.model.MoodSubType
import com.example.unfilteredapp.ui.theme.SanctuaryDesign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodSubSelectionScreen(
    modeType: String,
    onMoodSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val moods = remember(modeType) {
        MoodData.moods.filter { it.modeType == modeType }
    }
    
    val displayTitle = remember(modeType) {
        modeType.replace("_", " ").uppercase()
    }

    SanctuaryDesign.GlassyBackground {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            SanctuaryDesign.SanctuaryTopBar(
                title = displayTitle,
                subtitle = "Select the precise emotion that defines your current frequency.",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Editorial Header
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                Text(
                    "Identify your state.",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 44.sp,
                    letterSpacing = (-2).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Select the precise emotion in the $displayTitle quadrant.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            // Staggered Bento-style Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = moods,
                    key = { _, mood -> mood.subTitle + mood.modeType }
                ) { index, mood ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 50L) // Staggered entrance
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400)),
                        modifier = Modifier.animateItem()
                    ) {
                        MuseumMoodCard(
                            moodItem = mood,
                            onClick = { onMoodSelected("$modeType:${mood.subTitle}") }
                        )
                    }
                }
            }
        }
    }
    } // end GlassyBackground
}

@Composable
fun MuseumMoodCard(
    moodItem: MoodSubType,
    onClick: () -> Unit
) {
    val accentColor = remember(moodItem.lColor) {
        try {
            Color(android.graphics.Color.parseColor(moodItem.lColor))
        } catch (e: Exception) {
            Color(0xFF62F95D) // Default fallback
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    // Flat Museum Design: High contrast, No shadows, Rounded geometric shapes
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square grid for a modern look
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(32.dp))
            .background(accentColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Subtle indicator
            Surface(
                modifier = Modifier.size(8.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color.White.copy(alpha = 0.5f)
            ) {}

            Column {
                Text(
                    text = moodItem.subTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = moodItem.description.take(30) + if(moodItem.description.length > 30) "..." else "",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

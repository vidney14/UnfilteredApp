package com.example.unfilteredapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unfilteredapp.ui.theme.SanctuaryDesign
import com.example.unfilteredapp.ui.theme.MoodHighEnergyPleasantStart
import com.example.unfilteredapp.ui.theme.MoodHighEnergyPleasantEnd
import com.example.unfilteredapp.ui.theme.MoodLowEnergyPleasantStart
import com.example.unfilteredapp.ui.theme.MoodLowEnergyPleasantEnd
import com.example.unfilteredapp.ui.theme.MoodLowEnergyUnpleasantStart
import com.example.unfilteredapp.ui.theme.MoodLowEnergyUnpleasantEnd
import com.example.unfilteredapp.ui.theme.MoodHighEnergyUnpleasantStart
import com.example.unfilteredapp.ui.theme.MoodHighEnergyUnpleasantEnd
import com.example.unfilteredapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodCategoryScreen(
    onCategorySelected: (String) -> Unit,
    onViewAnalytics: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val categories = listOf(
        MoodCategoryItem(
            "High Energy\nPleasant",
            "Excited, Happy, Joyful",
            "high_energy_pleasant",
            MoodHighEnergyPleasantStart,
            MoodHighEnergyPleasantEnd,
            Icons.Default.ElectricBolt
        ),
        MoodCategoryItem(
            "Low Energy\nPleasant",
            "Calm, Relaxed, Content",
            "low_energy_pleasant",
            MoodLowEnergyPleasantStart,
            MoodLowEnergyPleasantEnd,
            Icons.Default.Favorite
        ),
        MoodCategoryItem(
            "Low Energy\nUnpleasant",
            "Sad, Bored, Fatigued",
            "low_energy_unpleasant",
            MoodLowEnergyUnpleasantStart,
            MoodLowEnergyUnpleasantEnd,
            Icons.Default.WaterDrop
        ),
        MoodCategoryItem(
            "High Energy\nUnpleasant",
            "Angry, Anxious, Stressed",
            "high_energy_unpleasant",
            MoodHighEnergyUnpleasantStart,
            MoodHighEnergyUnpleasantEnd,
            Icons.Default.SentimentDissatisfied
        )
    )

    SanctuaryDesign.GlassyBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SanctuaryDesign.SanctuaryTopBar(
                    title = "Hello, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "there"}",
                    subtitle = "How are you feeling today?",
                    actions = {
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout, 
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text = "Select a quadrant to explore your emotions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(categories) { index, category ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 100L)
                            visible = true
                        }
                        
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically { it / 2 }
                        ) {
                            CategoryCard(category) {
                                onCategorySelected(category.modeType)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // "Something Else" - Minimalist Glass Gradient Button (No Shadow)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { onViewAnalytics() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "DISCOVER TRENDS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class MoodCategoryItem(
    val title: String,
    val descriptor: String,
    val modeType: String,
    val startColor: Color,
    val endColor: Color,
    val icon: ImageVector
)

@Composable
fun CategoryCard(
    category: MoodCategoryItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(category.startColor, category.endColor)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp).size(28.dp)
                    )
                }

                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    ),
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = category.descriptor,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

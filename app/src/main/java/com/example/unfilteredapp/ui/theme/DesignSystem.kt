package com.example.unfilteredapp.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SanctuaryDesign {
    
    @Composable
    fun SanctuaryCard(
        modifier: Modifier = Modifier,
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        onClick: (() -> Unit)? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
            label = "scale"
        )

        Surface(
            modifier = modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            onClick = onClick
                        )
                    } else Modifier
                ),
            shape = RoundedCornerShape(24.dp),
            color = backgroundColor,
            tonalElevation = 2.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                content = content
            )
        }
    }

    @Composable
    fun SanctuaryTopBar(
        title: String,
        subtitle: String? = null,
        navigationIcon: ImageVector? = null,
        onNavigationClick: () -> Unit = {},
        actions: @Composable RowScope.() -> Unit = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                IconButton(
                    onClick = onNavigationClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(navigationIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }

    @Composable
    fun PrimaryButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        containerColor: Color = MaterialTheme.colorScheme.primary
    ) {
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = containerColor),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }

    @Composable
    fun GlassyBackground(
        color: Color = MaterialTheme.colorScheme.primary,
        content: @Composable BoxScope.() -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.08f), Color.Transparent),
                            radius = 1200f
                        )
                    )
            )
            content()
        }
    }
}

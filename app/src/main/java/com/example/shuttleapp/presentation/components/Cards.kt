package com.example.shuttleapp.presentation.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.ui.theme.AppleGreen
import com.example.shuttleapp.ui.theme.Lavender
import com.example.shuttleapp.ui.theme.NavyBlue
import com.example.shuttleapp.ui.theme.Orange
import com.example.shuttleapp.ui.theme.PowderBlue
import com.example.shuttleapp.ui.theme.SurfaceBg

// Gradient helpers — derived from the flat color but richer
private fun cardGradient(base: Color) = Brush.linearGradient(
    colors = listOf(
        base.copy(alpha = 1f),
        base.copy(red = (base.red * 0.78f).coerceIn(0f, 1f),
                  green = (base.green * 0.78f).coerceIn(0f, 1f),
                  blue = (base.blue * 0.78f).coerceIn(0f, 1f))
    )
)

@Composable
fun CardsContainer(
    lateShuttleCount: Int,
    unsyncedShuttleCount: Int,
    passengerCount: Int,
    currentRoute: String,
    tripType: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top row: two square stat cards side-by-side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SmallStatCard(
                modifier = Modifier.weight(1f),
                title = "Total Late Pass",
                value = "$lateShuttleCount",
                subtitle = if (lateShuttleCount == 0) "No late passes" else "$lateShuttleCount late shuttle recorded",
                gradient = cardGradient(Orange),
                icon = Icons.Filled.AssignmentLate
            )
            SmallStatCard(
                modifier = Modifier.weight(1f),
                title = "Unsynced Pass",
                value = "$unsyncedShuttleCount",
                subtitle = if (unsyncedShuttleCount == 0) "All synced ✓" else "Sync immediately",
                gradient = cardGradient(Lavender),
                icon = Icons.Filled.SyncProblem
            )
        }

        // Bottom two full-width cards
        FullWidthCard(
            title = "Current Route",
            value = if (currentRoute.isBlank()) "No Active Trip" else "$currentRoute — $tripType",
            subtitle = if (currentRoute.isBlank()) "No active trip at the moment"
            else "$tripType trip via $currentRoute route",
            gradient = cardGradient(PowderBlue),
            icon = Icons.Filled.Route
        )

        FullWidthCard(
            title = "Passenger Count",
            value = "$passengerCount",
            subtitle = if (passengerCount == 0) "Create a new shuttle pass to start recording"
            else "$passengerCount passenger${if (passengerCount != 1) "s" else ""} onboard — have a safe trip!",
            gradient = cardGradient(AppleGreen),
            icon = Icons.Filled.Group
        )
    }
}

@Composable
private fun SmallStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    gradient: Brush,
    icon: ImageVector
) {
    // Animate card entrance (scale-in)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = tween(durationMillis = 380, easing = EaseOutCubic),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .aspectRatio(1.25f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header row: title + icon bubble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title.uppercase(),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Big value number
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 44.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtle divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                )

                // Footer subtitle
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FullWidthCard(
    title: String,
    value: String,
    subtitle: String,
    gradient: Brush,
    icon: ImageVector
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = EaseOutCubic),
        label = "card_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(18.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = title.uppercase(),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Value
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                // Subtle separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                )

                Spacer(Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060d39)
@Composable
fun CardsContainerPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceBg) {
        CardsContainer(
            lateShuttleCount = 3,
            unsyncedShuttleCount = 1,
            passengerCount = 24,
            currentRoute = "Downtown Loop",
            tripType = "Express"
        )
    }
}

package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.ui.theme.ShuttleTheme

/** Stateless shuttle pass row. Same look as HomeScreen's ShuttlePassListItem, which moves to this in Phase 6. */
@Composable
fun ShuttlePassCard(item: ShuttlePassItemUi, onClick: () -> Unit) {
    val colors = ShuttleTheme.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isLate) colors.errorSoft else colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(if (item.isLate) colors.error else colors.success)
                )
                Spacer(Modifier.width(12.dp))
                PassDetails(item)
            }
            SyncPill(item.isSynced)
        }
    }
}

@Composable
private fun PassDetails(item: ShuttlePassItemUi) {
    val colors = ShuttleTheme.colors
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.isLate) {
                LateBadge()
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = item.schedule,
                color = if (item.isLate) colors.error else colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.code,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.routeAndTrip,
            color = colors.textSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LateBadge() {
    val colors = ShuttleTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.errorSoft)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.AccessTime,
                tint = colors.error,
                contentDescription = null,
                modifier = Modifier.size(10.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text("LATE", color = colors.error, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun SyncPill(isSynced: Boolean) {
    val colors = ShuttleTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSynced) colors.successSoft else colors.surfaceSubtle)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                tint = if (isSynced) colors.successText else colors.textDisabled,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (isSynced) "Synced" else "Unsynced",
                color = if (isSynced) colors.successText else colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShuttlePassCardPreview() {
    ShuttlePassCard(
        item = ShuttlePassItemUi(
            id = "1",
            code = "SPT-2025-01-0A1B2C3D4E5F",
            schedule = "2025-01-02  06:15 — 07:05",
            routeAndTrip = "Villa Estrella · Incoming",
            isLate = true,
            isSynced = false
        ),
        onClick = {}
    )
}

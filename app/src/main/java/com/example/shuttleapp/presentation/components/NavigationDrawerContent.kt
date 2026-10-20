package com.example.shuttleapp.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.R
import com.example.shuttleapp.presentation.components.home.MapLines
import com.example.shuttleapp.presentation.mapper.DashboardFormat
import com.example.shuttleapp.presentation.state.DrawerUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/**
 * Midnight account menu (mockup 04), opened only from the Home avatar. Home, Passes and Sync live in the
 * bottom bar, so the drawer carries the profile, a sync card, Settings and Sign out.
 */
@Composable
fun DrawerContent(
    ui: DrawerUi,
    onClose: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    // Scrolls on short screens; otherwise the min height pins the skyline to the bottom.
    BoxWithConstraints(modifier.fillMaxSize().background(colors.brandBand)) {
        MapLines(Modifier.fillMaxWidth().height(260.dp))
        Column(Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight)) {
            DrawerItems(ui, onClose, onSyncNow, onOpenSettings, onSignOut)
        }
    }
}

@Composable
private fun ColumnScope.DrawerItems(
    ui: DrawerUi,
    onClose: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    val colors = ShuttleTheme.colors
    BrandRow(onClose)
    DrawerProfile(ui)
    SyncCard(ui, onSyncNow, Modifier.padding(horizontal = Dimens.SpaceM))
    Spacer(Modifier.padding(top = Dimens.SpaceS))
    DrawerItem(Icons.Filled.Settings, "Settings", onOpenSettings, trailing = "Appearance")
    HorizontalDivider(Modifier.padding(horizontal = Dimens.PageMargin), color = colors.onBrandBand.copy(alpha = 0.12f))
    DrawerItem(Icons.AutoMirrored.Outlined.Logout, "Sign out", onSignOut)
    Spacer(Modifier.weight(1f))
    if (ui.versionLine.isNotBlank()) {
        Text(
            ui.versionLine,
            style = ShuttleType.Caption,
            color = colors.onBrandBandMuted,
            modifier = Modifier.padding(horizontal = Dimens.PageMargin, vertical = Dimens.SpaceM)
        )
    }
    DrawerSkyline()
}

/** Compact brand line with the close button, below the status bar. */
@Composable
private fun BrandRow(onClose: () -> Unit) {
    val colors = ShuttleTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = Dimens.PageMargin, end = Dimens.SpaceM, top = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_shuttle_van_logo),
            contentDescription = null,
            tint = colors.onBrandBand,
            modifier = Modifier.size(width = 44.dp, height = 22.dp)
        )
        Text(
            "SHUTTLE MOBILE APP",
            style = ShuttleType.Overline.copy(fontSize = 13.sp, letterSpacing = 1.6.sp),
            color = colors.onBrandBand,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = Dimens.SpaceM).weight(1f)
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(Dimens.MinTouch)
                .clip(CircleShape)
                .background(colors.onBrandBand.copy(alpha = 0.10f))
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Close menu", tint = colors.onBrandBand)
        }
    }
}

@Composable
private fun DrawerProfile(ui: DrawerUi) {
    val colors = ShuttleTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.PageMargin, vertical = Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.surfaceQuiet)
                .border(3.dp, colors.onBrandBand.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(ui.initials.ifBlank { "?" }, style = ShuttleType.Section, color = colors.textPrimary)
        }
        Column(Modifier.weight(1f)) {
            Text(
                ui.name,
                style = ShuttleType.Section,
                color = colors.onBrandBand,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() }
            )
            if (ui.employeeNo.isNotBlank()) {
                Text(ui.employeeNo, style = ShuttleType.Body, color = colors.onBrandBandMuted, maxLines = 1)
            }
            if (ui.providerName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = colors.success, modifier = Modifier.size(16.dp))
                    Text(
                        ui.providerName,
                        style = ShuttleType.Body,
                        color = colors.onBrandBandMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = Dimens.SpaceXs)
                    )
                }
            }
        }
    }
}

/** Pending count, last sync time and a Mint "Sync now" (only while something is waiting). */
@Composable
private fun SyncCard(ui: DrawerUi, onSyncNow: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    val pending = ui.pendingCount > 0
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusCard))
            .background(colors.onBrandBand.copy(alpha = 0.06f))
            .border(1.dp, colors.onBrandBand.copy(alpha = 0.10f), RoundedCornerShape(Dimens.RadiusCard))
            .padding(Dimens.SpaceM)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
            Icon(
                if (pending) Icons.Rounded.CloudUpload else Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (pending) colors.warning else colors.success,
                modifier = Modifier.size(28.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    DashboardFormat.pendingShort(ui.pendingCount),
                    style = ShuttleType.Label.copy(fontSize = 17.sp),
                    color = colors.onBrandBand
                )
                if (ui.lastSynced.isNotBlank()) {
                    Text(ui.lastSynced, style = ShuttleType.Caption, color = colors.onBrandBandMuted)
                }
            }
        }
        if (pending) {
            Button(
                onClick = onSyncNow,
                enabled = !ui.isSyncing,
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpaceM).heightIn(min = Dimens.CtaHeight),
                shape = RoundedCornerShape(Dimens.RadiusButton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.success,
                    contentColor = colors.onSuccess,
                    disabledContainerColor = colors.success.copy(alpha = 0.6f),
                    disabledContentColor = colors.onSuccess
                ),
                contentPadding = PaddingValues(horizontal = Dimens.SpaceL)
            ) {
                if (ui.isSyncing) {
                    CircularProgressIndicator(color = colors.onSuccess, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(22.dp))
                }
                Text(
                    if (ui.isSyncing) "Syncing…" else "Sync now",
                    style = ShuttleType.Button,
                    modifier = Modifier.padding(start = Dimens.SpaceS)
                )
            }
        }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit, trailing: String? = null) {
    val colors = ShuttleTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceS)
            .clip(RoundedCornerShape(Dimens.RadiusButton))
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
    ) {
        Icon(icon, contentDescription = null, tint = colors.onBrandBand, modifier = Modifier.size(24.dp))
        Text(
            label,
            style = ShuttleType.BodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onBrandBand,
            modifier = Modifier.weight(1f)
        )
        trailing?.let { Text(it, style = ShuttleType.Caption, color = colors.onBrandBandMuted) }
    }
}

private val previewUi = DrawerUi(
    name = "Christian Losamia",
    employeeNo = "Employee no. 15125112",
    providerName = "SEMPHIL Shuttle Services",
    initials = "CL",
    pendingCount = 3,
    lastSynced = "Last synced today, 10:42",
    versionLine = "Version 1.0 · Offline data saved on this device"
)

@Preview(widthDp = 320, heightDp = 760)
@Preview(widthDp = 320, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DrawerContentPreview() = ShuttleAppTheme {
    DrawerContent(ui = previewUi, onClose = {}, onSyncNow = {}, onOpenSettings = {}, onSignOut = {})
}

@Preview(widthDp = 320, heightDp = 640)
@Composable
private fun DrawerContentAllSyncedLongNamePreview() = ShuttleAppTheme(darkTheme = false) {
    DrawerContent(
        ui = previewUi.copy(
            name = "Maria Concepcion Villanueva-Dela Cruz",
            employeeNo = "",
            providerName = "",
            initials = "MV",
            pendingCount = 0
        ),
        onClose = {},
        onSyncNow = {},
        onOpenSettings = {},
        onSignOut = {}
    )
}

@Preview(widthDp = 320, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DrawerContentSyncingPreview() = ShuttleAppTheme {
    DrawerContent(ui = previewUi.copy(isSyncing = true), onClose = {}, onSyncNow = {}, onOpenSettings = {}, onSignOut = {})
}

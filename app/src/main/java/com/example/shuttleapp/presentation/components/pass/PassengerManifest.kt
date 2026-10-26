package com.example.shuttleapp.presentation.components.pass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuttleapp.presentation.components.ConfirmBasicDialog
import com.example.shuttleapp.presentation.state.PassengerRowUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

@Composable
fun ManifestHeader(count: Int, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.fillMaxWidth().padding(horizontal = Dimens.SpaceS)
    ) {
        Text("Passenger manifest", style = ShuttleType.Section, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(
            text = if (count == 1) "1 passenger" else "$count passengers",
            style = ShuttleType.Label.copy(fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum"),
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 3.dp)
        )
    }
}

/**
 * One manifest row. Rows are meant to be emitted as separate lazy items; [isFirst] / [isLast] round the
 * corners so consecutive rows read as one card. Pass null callbacks for a read-only (finalized) pass.
 * [highlighted] tints the row mint, for the passenger who just boarded on the scanner.
 */
@Composable
fun PassengerManifestRow(
    row: PassengerRowUi,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val colors = ShuttleTheme.colors
    val corner = Dimens.RadiusCard
    val shape = RoundedCornerShape(
        topStart = if (isFirst) corner else 0.dp, topEnd = if (isFirst) corner else 0.dp,
        bottomStart = if (isLast) corner else 0.dp, bottomEnd = if (isLast) corner else 0.dp
    )
    Column(modifier.fillMaxWidth().clip(shape).background(if (highlighted) colors.successSoft else colors.surface)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 14.dp, end = if (onEdit == null) Dimens.SpaceM else Dimens.SpaceXs)
        ) {
            NumberBadge(row.number, highlighted)
            Text(
                text = row.code,
                style = ShuttleType.Body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                row.time,
                style = ShuttleType.Label.copy(fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum"),
                color = if (highlighted) colors.textPrimary else colors.textSecondary
            )
            if (onEdit != null && onDelete != null) RowMenu(row, onEdit, onDelete)
        }
        if (!isLast) HorizontalDivider(color = colors.divider, modifier = Modifier.padding(start = 60.dp))
    }
}

@Composable
private fun NumberBadge(number: Int, highlighted: Boolean) {
    val colors = ShuttleTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(32.dp).clip(CircleShape).background(if (highlighted) colors.success else colors.surfaceQuiet)
    ) {
        Text(
            text = number.toString(),
            style = ShuttleType.Label.copy(fontFeatureSettings = "tnum"),
            color = if (highlighted) colors.onSuccess else colors.brandAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 16.dp)
        )
    }
}

/** ⋮ menu with Edit and Remove; removing asks for confirmation first. */
@Composable
private fun RowMenu(row: PassengerRowUi, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = ShuttleTheme.colors
    var expanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Options for passenger ${row.number}", tint = colors.textSecondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = colors.surface) {
            DropdownMenuItem(
                text = { Text("Edit QR code", style = ShuttleType.Body, color = colors.textPrimary) },
                onClick = { expanded = false; onEdit() }
            )
            DropdownMenuItem(
                text = { Text("Remove passenger", style = ShuttleType.Body, color = colors.error) },
                onClick = { expanded = false; confirmDelete = true }
            )
        }
    }
    if (confirmDelete) {
        ConfirmBasicDialog(
            content = "Remove passenger ${row.number} (${row.code}) from this pass?",
            onDismiss = { confirmDelete = false },
            onConfirm = onDelete
        )
    }
}

@Composable
fun ManifestEmptyState(modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(horizontal = Dimens.SpaceL)
    ) {
        SeatIllustration(Modifier.padding(top = Dimens.SpaceS).widthIn(max = 188.dp).fillMaxWidth())
        Text(
            "No passengers yet",
            style = ShuttleType.Section.copy(fontSize = 18.sp, lineHeight = 26.sp),
            color = colors.textPrimary,
            modifier = Modifier.padding(top = Dimens.SpaceS + Dimens.SpaceXs)
        )
        Text(
            "Scan each passenger's QR code as they board.",
            style = ShuttleType.Body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.SpaceXs)
        )
    }
}

package com.example.shuttleapp.presentation.components.pass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.components.ExpandedDropDown
import com.example.shuttleapp.presentation.state.CreatePassFormUi
import com.example.shuttleapp.presentation.state.TRIP_INCOMING
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

/** Heading shown on the brand band above the create form. */
@Composable
fun CreatePassIntro(modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Column(modifier.fillMaxWidth().padding(horizontal = Dimens.PageMargin)) {
        Text("Set up today's trip", style = ShuttleType.Heading2, color = colors.onBrandBand)
        Text(
            "Pick the route, direction and shuttle. You'll scan passengers next.",
            style = ShuttleType.Body,
            color = colors.onBrandBandMuted,
            modifier = Modifier.padding(top = Dimens.SpaceXs)
        )
    }
}

@Composable
fun CreatePassForm(
    form: CreatePassFormUi,
    onRouteChanged: (String) -> Unit,
    onTripTypeSelected: (String) -> Unit,
    onPlateNumberChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ShuttleTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(Dimens.SpaceM), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
            FormField("Route", form.routeError) {
                ExpandedDropDown(
                    label = "Select a route",
                    categories = form.routes,
                    initialValue = form.route,
                    errorIndicator = form.routeError != null,
                    onValueChanged = onRouteChanged,
                    shape = RoundedCornerShape(Dimens.RadiusButton),
                    leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null, tint = colors.textSecondary) }
                )
            }
            FormField("Trip type", form.tripTypeError ?: form.tripTypeHint, isError = form.tripTypeError != null) {
                TripTypeSegmented(form.tripTypes, form.tripType, onTripTypeSelected)
            }
            FormField("Plate number", form.plateNumberError) {
                ExpandedDropDown(
                    label = "Select a shuttle",
                    categories = form.plateNumbers,
                    initialValue = form.plateNumber,
                    errorIndicator = form.plateNumberError != null,
                    onValueChanged = onPlateNumberChanged,
                    shape = RoundedCornerShape(Dimens.RadiusButton),
                    leadingIcon = { Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = colors.textSecondary) }
                )
            }
        }
    }
}

/** Label above the control; [supporting] below it is an error when [isError], otherwise helper text. */
@Composable
private fun FormField(
    label: String,
    supporting: String?,
    isError: Boolean = supporting != null,
    content: @Composable () -> Unit
) {
    val colors = ShuttleTheme.colors
    Column {
        Text(label, style = ShuttleType.Label, color = colors.textPrimary, modifier = Modifier.padding(bottom = 6.dp))
        content()
        supporting?.let {
            Text(
                it,
                style = ShuttleType.Caption,
                color = if (isError) colors.error else colors.textSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun TripTypeSegmented(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val colors = ShuttleTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.MinTouch)
            .clip(RoundedCornerShape(Dimens.RadiusButton))
            .background(colors.surfaceSubtle)
            .padding(4.dp)
            .selectableGroup()
    ) {
        options.forEach { option ->
            val isSelected = option.equals(selected, ignoreCase = true)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.MinTouch - 8.dp)
                    .then(if (isSelected) Modifier.shadow(1.dp, RoundedCornerShape(9.dp)) else Modifier)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) colors.surface else colors.surfaceSubtle)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) })
            ) {
                val tint = if (isSelected) colors.textPrimary else colors.textSecondary
                Icon(tripIcon(option), contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                Text(
                    option,
                    style = ShuttleType.Label.copy(fontWeight = FontWeight.SemiBold),
                    color = tint,
                    modifier = Modifier.padding(start = Dimens.SpaceS)
                )
            }
        }
    }
}

private fun tripIcon(option: String): ImageVector =
    if (option.equals(TRIP_INCOMING, ignoreCase = true)) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout

/** Dashed row that previews the pass as fields are picked. */
@Composable
fun CreatePassPreview(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val colors = ShuttleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = colors.border,
                    cornerRadius = CornerRadius(Dimens.RadiusCard.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())))
                )
            }
            .padding(horizontal = Dimens.SpaceM, vertical = 14.dp)
    ) {
        MiniTimeline()
        Column(Modifier.padding(start = 14.dp)) {
            Text(title, style = ShuttleType.Body.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = ShuttleType.Caption, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MiniTimeline() {
    val colors = ShuttleTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(colors.surface).border(2.5.dp, colors.successText, CircleShape))
        Canvas(Modifier.size(width = 2.dp, height = 14.dp)) {
            drawLine(
                colors.border, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height),
                strokeWidth = size.width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 3.dp.toPx()))
            )
        }
        Box(Modifier.size(10.dp).clip(CircleShape).background(colors.primary))
    }
}

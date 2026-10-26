package com.example.shuttleapp.presentation.components.pass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shuttleapp.presentation.state.PassCardUi
import com.example.shuttleapp.presentation.state.PassengerRowUi
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme

/**
 * The pass card overlapping the brand band, then the passenger manifest. Shared by the current-pass
 * screen (editable rows) and the pass detail screen (read-only: pass null callbacks).
 */
@Composable
fun PassDetailsList(
    card: PassCardUi,
    passengers: List<PassengerRowUi>,
    onEditPassenger: ((String) -> Unit)?,
    onDeletePassenger: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val colors = ShuttleTheme.colors
    LazyColumn(modifier.fillMaxSize(), contentPadding = contentPadding) {
        item(key = "card") {
            Box {
                Box(Modifier.fillMaxWidth().height(48.dp).background(colors.brandBand))
                PassSummaryCard(card, Modifier.padding(start = Dimens.SpaceM, end = Dimens.SpaceM, top = Dimens.SpaceXs))
            }
        }
        item(key = "header") {
            ManifestHeader(
                passengers.size,
                Modifier.padding(start = Dimens.SpaceM, end = Dimens.SpaceM, top = Dimens.SpaceL, bottom = 10.dp)
            )
        }
        if (passengers.isEmpty()) {
            item(key = "empty") { ManifestEmptyState(Modifier.padding(top = Dimens.SpaceS, bottom = Dimens.SpaceL)) }
        } else {
            itemsIndexed(passengers, key = { _, row -> row.id }) { index, row ->
                PassengerManifestRow(
                    row = row,
                    isFirst = index == 0,
                    isLast = index == passengers.lastIndex,
                    onEdit = onEditPassenger?.let { { it(row.id) } },
                    onDelete = onDeletePassenger?.let { { it(row.id) } },
                    modifier = Modifier.padding(horizontal = Dimens.SpaceM)
                )
            }
        }
        item(key = "end") { Box(Modifier.height(Dimens.SpaceM)) }
    }
}

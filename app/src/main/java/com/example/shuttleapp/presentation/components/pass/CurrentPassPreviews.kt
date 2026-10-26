package com.example.shuttleapp.presentation.components.pass

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.shuttleapp.presentation.CurrentPassScreen
import com.example.shuttleapp.presentation.mapper.CurrentPassUiMapper
import com.example.shuttleapp.presentation.state.CreatePassFormUi
import com.example.shuttleapp.presentation.state.CurrentPassMode
import com.example.shuttleapp.presentation.state.CurrentPassState
import com.example.shuttleapp.presentation.state.PassActionUi
import com.example.shuttleapp.presentation.state.PassCardUi
import com.example.shuttleapp.presentation.state.PassStatus
import com.example.shuttleapp.presentation.state.PassengerRowUi
import com.example.shuttleapp.ui.theme.ShuttleAppTheme

private val draftCard = PassCardUi(
    status = PassStatus.NotDeparted,
    dateLabel = "Tue, Oct 20, 2026",
    fromLabel = "Villa Estrella", fromIsGeneric = false,
    toLabel = CurrentPassUiMapper.DROP_OFF_POINT, toIsGeneric = true,
    departure = CurrentPassUiMapper.TIME_UNSET, hasDeparture = false,
    arrival = CurrentPassUiMapper.TIME_UNSET, hasArrival = false,
    plateNumber = "ABDA 3332", provider = "BCTSS", driver = "Christian Losamia"
)

private val passengers = (6 downTo 1).map { n ->
    PassengerRowUi(id = "$n", number = n, code = "Passenger_1512${5112 - n * 37}", time = "16:${14 + n * 2}")
}

private val createState = CurrentPassState(
    mode = CurrentPassMode.Create,
    form = CurrentPassUiMapper.withPreview(
        CreatePassFormUi(route = "Villa Estrella", tripType = "Incoming", plateNumberError = "Plate number can't be blank")
    )
)

private val emptyState = CurrentPassState(
    mode = CurrentPassMode.Active("p1", draftCard, emptyList(), PassActionUi.StartDeparture)
)

private val manifestState = CurrentPassState(
    mode = CurrentPassMode.Active(
        "p1",
        draftCard.copy(status = PassStatus.InTransit, departure = "16:30", hasDeparture = true),
        passengers,
        PassActionUi.Complete(enabled = true, hint = null)
    )
)

@Composable
private fun Screen(state: CurrentPassState, dark: Boolean = false) = ShuttleAppTheme(darkTheme = dark) {
    CurrentPassScreen(state, remember { SnackbarHostState() }, onAction = {})
}

@Preview(name = "06a Create", widthDp = 412, heightDp = 915)
@Composable
private fun CreatePreview() = Screen(createState)

@Preview(name = "06 Empty pass", widthDp = 412, heightDp = 915)
@Composable
private fun EmptyPreview() = Screen(emptyState)

@Preview(name = "07 Manifest", widthDp = 412, heightDp = 915)
@Composable
private fun ManifestPreview() = Screen(manifestState)

@Preview(name = "07 Manifest, narrow", widthDp = 360, heightDp = 780)
@Composable
private fun ManifestNarrowPreview() = Screen(manifestState)

@Preview(name = "06a Create dark", widthDp = 412, heightDp = 915)
@Composable
private fun CreateDarkPreview() = Screen(createState, dark = true)

@Preview(name = "06 Empty pass dark", widthDp = 412, heightDp = 915)
@Composable
private fun EmptyDarkPreview() = Screen(emptyState, dark = true)

@Preview(name = "07 Manifest dark", widthDp = 412, heightDp = 915)
@Composable
private fun ManifestDarkPreview() = Screen(manifestState, dark = true)

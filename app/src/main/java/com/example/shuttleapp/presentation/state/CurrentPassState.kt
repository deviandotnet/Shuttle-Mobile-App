package com.example.shuttleapp.presentation.state

/** Everything the current-pass screen shows: the create form, or the open draft and its manifest. */
data class CurrentPassState(
    val mode: CurrentPassMode = CurrentPassMode.Loading,
    val form: CreatePassFormUi = CreatePassFormUi(),
    /** Non-null while the edit-passenger dialog is open. */
    val editingPassenger: EditPassengerUi? = null,
    val showDeleteConfirm: Boolean = false,
    val isSubmitting: Boolean = false
)

sealed interface CurrentPassMode {
    data object Loading : CurrentPassMode
    data object Create : CurrentPassMode
    data class Active(
        val passId: String,
        val card: PassCardUi,
        val passengers: List<PassengerRowUi>,
        val action: PassActionUi
    ) : CurrentPassMode
}

data class CreatePassFormUi(
    val route: String = "",
    val tripType: String = "",
    val plateNumber: String = "",
    val routeError: String? = null,
    val tripTypeError: String? = null,
    val plateNumberError: String? = null,
    val routes: List<String> = emptyList(),
    val plateNumbers: List<String> = emptyList(),
    val tripTypes: List<String> = listOf(TRIP_INCOMING, TRIP_OUTGOING),
    val tripTypeHint: String? = null,
    val previewTitle: String = "",
    val previewSubtitle: String = ""
)

/** The pass summary card, already formatted for display. */
data class PassCardUi(
    val status: PassStatus,
    val dateLabel: String,
    val fromLabel: String,
    /** True when the stop is a generic placeholder ("Pick-up point") rather than a real place. */
    val fromIsGeneric: Boolean,
    val toLabel: String,
    val toIsGeneric: Boolean,
    val departure: String,
    val hasDeparture: Boolean,
    val arrival: String,
    val hasArrival: Boolean,
    val plateNumber: String,
    val provider: String,
    val driver: String
)

enum class PassStatus(val label: String) {
    NotDeparted("Not departed"),
    InTransit("In transit"),
    Completed("Completed")
}

data class PassengerRowUi(
    val id: String,
    val number: Int,
    val code: String,
    val time: String
)

sealed interface PassActionUi {
    data object StartDeparture : PassActionUi
    data class Complete(val enabled: Boolean, val hint: String?) : PassActionUi
    /** Finalized passes are read-only. */
    data object None : PassActionUi
}

data class EditPassengerUi(
    val passengerId: String,
    val qrCode: String,
    val error: String? = null
)

const val TRIP_INCOMING = "Incoming"
const val TRIP_OUTGOING = "Outgoing"

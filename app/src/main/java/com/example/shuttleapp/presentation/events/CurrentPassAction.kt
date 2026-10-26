package com.example.shuttleapp.presentation.events

sealed interface CurrentPassAction {
    // Create form
    data class RouteChanged(val route: String) : CurrentPassAction
    data class TripTypeSelected(val tripType: String) : CurrentPassAction
    data class PlateNumberChanged(val plateNumber: String) : CurrentPassAction
    data object CreatePass : CurrentPassAction

    // Open draft
    data object AddPassenger : CurrentPassAction
    data object EditPass : CurrentPassAction
    data object RequestDeletePass : CurrentPassAction
    data object ConfirmDeletePass : CurrentPassAction
    data object DismissDeletePass : CurrentPassAction
    data object StartDeparture : CurrentPassAction
    data object CompletePass : CurrentPassAction

    // Passengers
    data class EditPassenger(val passengerId: String) : CurrentPassAction
    data class EditedQrChanged(val qrCode: String) : CurrentPassAction
    data object SaveEditedPassenger : CurrentPassAction
    data object CancelEditPassenger : CurrentPassAction
    data class DeletePassenger(val passengerId: String) : CurrentPassAction

    data object Back : CurrentPassAction
}

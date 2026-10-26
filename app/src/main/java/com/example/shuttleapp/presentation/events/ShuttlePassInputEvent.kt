package com.example.shuttleapp.presentation.events

sealed class ShuttlePassInputEvent {

    data class ShuttleProviderChanged(val shuttleProvider: String) : ShuttlePassInputEvent()
    data class DriverChanged(val driver: String) : ShuttlePassInputEvent()
    data class PlateNumberChanged(val plateNumber: String) : ShuttlePassInputEvent()
    data class RouteChanged(val route: String) : ShuttlePassInputEvent()
    data class TripTypeChanged(val trip: String) : ShuttlePassInputEvent()
    data class DepartureChanged(val departure: String) : ShuttlePassInputEvent()
    data class DateChanged(val date: String) : ShuttlePassInputEvent()
    data class ArrivalChanged(val arrival: String) : ShuttlePassInputEvent()

}
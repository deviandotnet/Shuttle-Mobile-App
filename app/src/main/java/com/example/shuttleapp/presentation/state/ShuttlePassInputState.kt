package com.example.shuttleapp.presentation.state

data class ShuttlePassInputState(

    var shuttleProvider: String = "",
    var shuttleProviderErrorMessage: String? = null,
    var driver: String = "",
    var driverErrorMessage: String? = null,
    var plateNumber: String = "",
    var plateNumberErrorMessage: String? = null,
    var route: String = "",
    var routeErrorMessage: String? = null,
    var tripType: String = "",
    var tripTypeErrorMessage: String? = null,
    var departure: String = "",
    var departureErrorMessage: String? = null,
    var arrival: String = "",
    var arrivalErrorMessage: String? = null,
    var date: String = "",
    var dateErrorMessage: String? = null

)

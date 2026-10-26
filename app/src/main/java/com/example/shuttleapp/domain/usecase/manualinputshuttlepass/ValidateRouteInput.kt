package com.example.shuttleapp.domain.usecase.manualinputshuttlepass

import com.example.shuttleapp.domain.usecase.ShuttlePassValidationResult

class ValidateRouteInput {
    fun execute(route: String, routes: List<String>) : ShuttlePassValidationResult {

        if(route.isBlank()) {
            return ShuttlePassValidationResult(
                successful = false,
                errorMessage = "Route can't be blank"
            )
        }
        if(!routes.any { it.equals(route, ignoreCase = true) }) {
            return ShuttlePassValidationResult(
                successful = false,
                errorMessage = "Route does not exist"
            )
        }

        return ShuttlePassValidationResult(
            successful = true
        )
    }
}
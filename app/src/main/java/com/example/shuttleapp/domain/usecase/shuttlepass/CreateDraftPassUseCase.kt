package com.example.shuttleapp.domain.usecase.shuttlepass

import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.domain.repository.ShuttleRepository
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidatePlateNumberInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateRouteInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateTripTypeInput
import com.example.shuttleapp.util.Resource
import com.example.shuttleapp.util.displayDate
import kotlinx.coroutines.CancellationException
import java.util.UUID

/** What the driver picked on the create form. Route and plate are display names, not ids. */
data class CreatePassInput(
    val route: String,
    val tripType: String,
    val plateNumber: String,
    val driverId: String,
    val providerId: String
)

sealed interface CreatePassResult {
    data class Invalid(
        val routeError: String?,
        val tripTypeError: String?,
        val plateNumberError: String?
    ) : CreatePassResult

    data class Created(val pass: ShuttlePass) : CreatePassResult
    data class Failed(val message: String) : CreatePassResult
}

/** Validates the create form and saves a new draft pass (offline, in Room). */
class CreateDraftPassUseCase(
    private val shuttlePassRepository: ShuttlePassRepository,
    private val routesRepository: RoutesRepository,
    private val shuttleRepository: ShuttleRepository,
    private val validateRouteInput: ValidateRouteInput,
    private val validateTripTypeInput: ValidateTripTypeInput,
    private val validatePlateNumberInput: ValidatePlateNumberInput
) {

    suspend operator fun invoke(
        input: CreatePassInput,
        routes: List<String>,
        plateNumbers: List<String>
    ): CreatePassResult {
        val route = validateRouteInput.execute(input.route, routes)
        val tripType = validateTripTypeInput.execute(input.tripType)
        val plate = validatePlateNumberInput.execute(input.plateNumber, plateNumbers)
        if (!route.successful || !tripType.successful || !plate.successful) {
            return CreatePassResult.Invalid(route.errorMessage, tripType.errorMessage, plate.errorMessage)
        }

        // A pass without its driver or provider can never sync, so don't save one.
        if (input.driverId.isBlank() || input.providerId.isBlank()) {
            return CreatePassResult.Failed("Your profile is still loading. Try again in a moment.")
        }

        // Validation ignores case but the id lookups match exactly, so look up the stored spelling.
        val routeName = routes.first { it.equals(input.route, ignoreCase = true) }
        val plateNumber = plateNumbers.first { it.equals(input.plateNumber, ignoreCase = true) }
        val notFound = CreatePassResult.Failed("Couldn't find that route or shuttle on this device. Please try again.")
        val (routeId, shuttleId) = try {
            routesRepository.getRouteIdByName(routeName) to shuttleRepository.getShuttleIdByPlateNumber(plateNumber)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            return notFound
        }
        if (routeId.isBlank() || shuttleId.isBlank()) return notFound

        val pass = ShuttlePass(
            id = UUID.randomUUID().toString(),
            routeId = routeId,
            plateNumber = shuttleId,
            provider = input.providerId,
            driver = input.driverId,
            date = displayDate(),
            dateCreated = System.currentTimeMillis(),
            tripType = input.tripType,
            isDraft = true
        )

        return when (val result = shuttlePassRepository.insert(pass)) {
            is Resource.Error -> CreatePassResult.Failed(result.message ?: "Couldn't save the shuttle pass.")
            else -> CreatePassResult.Created(pass)
        }
    }
}

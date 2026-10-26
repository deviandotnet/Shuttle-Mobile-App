package com.example.shuttleapp.domain.usecase.shuttlepass

import com.example.shuttleapp.domain.model.Route
import com.example.shuttleapp.domain.model.Shuttle
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidatePlateNumberInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateRouteInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateTripTypeInput
import com.example.shuttleapp.testutil.FakeRoutesRepository
import com.example.shuttleapp.testutil.FakeShuttlePassRepository
import com.example.shuttleapp.testutil.FakeShuttleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateDraftPassUseCaseTest {

    private val passes = FakeShuttlePassRepository()
    private val useCase = CreateDraftPassUseCase(
        shuttlePassRepository = passes,
        routesRepository = FakeRoutesRepository(routes = listOf(Route("VE", "r1", "Villa Estrella"))),
        shuttleRepository = FakeShuttleRepository(listOf(Shuttle("s1", "ABDA 3332", "prov1"))),
        validateRouteInput = ValidateRouteInput(),
        validateTripTypeInput = ValidateTripTypeInput(),
        validatePlateNumberInput = ValidatePlateNumberInput()
    )
    private val routes = listOf("Villa Estrella")
    private val plates = listOf("ABDA 3332")

    @Test
    fun `route and plate typed in another case resolve to their ids`() = runTest {
        val result = useCase(CreatePassInput("villa estrella", "Incoming", "abda 3332", "d1", "prov1"), routes, plates)

        val pass = (result as CreatePassResult.Created).pass
        assertEquals("r1", pass.routeId)
        assertEquals("s1", pass.plateNumber)
        assertEquals(pass, passes.inserted.single())
    }

    @Test
    fun `a blank provider saves nothing`() = runTest {
        val result = useCase(CreatePassInput("Villa Estrella", "Incoming", "ABDA 3332", "d1", ""), routes, plates)

        assertTrue(result is CreatePassResult.Failed)
        assertTrue(passes.inserted.isEmpty())
    }

    @Test
    fun `invalid input reports each field and saves nothing`() = runTest {
        val result = useCase(CreatePassInput("Nowhere", "", "", "d1", "prov1"), routes, plates)

        val invalid = result as CreatePassResult.Invalid
        assertEquals("Route does not exist", invalid.routeError)
        assertEquals("Plate number can't be blank", invalid.plateNumberError)
        assertTrue(passes.inserted.isEmpty())
    }
}

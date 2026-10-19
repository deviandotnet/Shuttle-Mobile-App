package com.example.shuttleapp.presentation.mapper

import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.util.formatMonthYear
import com.example.shuttleapp.util.formatTimeHHmm
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Formats shuttle passes for list rows (dashboard and pass list), resolving each route name once.
 * Not a singleton: each ViewModel gets its own instance, so the cache lives as long as that screen.
 */
class ShuttlePassItemUiMapper @Inject constructor(
    private val routesRepository: RoutesRepository
) {
    private val routeNames = mutableMapOf<String, String>()

    /** Newest first, as both lists display them. */
    suspend fun toUi(passes: List<ShuttlePassWithPassenger>): List<ShuttlePassItemUi> =
        passes.sortedByDescending { it.shuttlePass.dateCreated }.map { toUi(it) }

    suspend fun toUi(item: ShuttlePassWithPassenger): ShuttlePassItemUi {
        val pass = item.shuttlePass
        return ShuttlePassItemUi(
            id = pass.id,
            code = "SPT-${formatMonthYear(pass.dateCreated)}-${pass.id.takeLast(12).uppercase()}",
            schedule = "${pass.date}  ${formatTimeHHmm(pass.departure)} — ${formatTimeHHmm(pass.arrival)}",
            routeAndTrip = "${routeName(pass.routeId)} · ${pass.tripType}",
            isLate = pass.isLateShuttle,
            isSynced = pass.isSynced
        )
    }

    private suspend fun routeName(routeId: String): String {
        routeNames[routeId]?.let { return it }
        // A missing route shows a blank name rather than failing the whole list; it is retried next emission.
        return try {
            routesRepository.getRouteNameById(routeId).also { routeNames[routeId] = it }
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            ""
        }
    }
}

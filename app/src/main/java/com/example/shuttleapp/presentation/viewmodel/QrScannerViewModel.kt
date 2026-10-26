package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.domain.usecase.shuttlepass.BoardPassengerUseCase
import com.example.shuttleapp.domain.usecase.shuttlepass.BoardingResult
import com.example.shuttleapp.presentation.events.QrScannerAction
import com.example.shuttleapp.presentation.events.QrScannerEvent
import com.example.shuttleapp.presentation.events.ScanFeedback
import com.example.shuttleapp.presentation.mapper.CurrentPassUiMapper
import com.example.shuttleapp.presentation.mapper.QrScannerUiMapper
import com.example.shuttleapp.presentation.state.CameraAccess
import com.example.shuttleapp.presentation.state.QrScannerState
import com.example.shuttleapp.presentation.state.ScanResultUi
import com.example.shuttleapp.presentation.state.ScannerTripUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Scans passengers onto a draft pass. Route argument "id" is the shuttle pass id. The camera and decoder
 * live in the UI; this only receives decoded QR text.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QrScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    shuttlePassRepository: ShuttlePassRepository,
    passengerQRRepository: PassengerQRRepository,
    private val routesRepository: RoutesRepository,
    private val boardPassenger: BoardPassengerUseCase
) : ViewModel() {

    private val passId: String = savedStateHandle["id"] ?: ""

    private val _events = Channel<QrScannerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val input = MutableStateFlow(Input())

    private val passengers: StateFlow<List<PassengerQR>> = passengerQRRepository.getDraftedPassengers(passId)
        .catch {
            send(QrScannerEvent.ShowMessage("Couldn't load the passengers on this pass."))
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val trip: Flow<ScannerTripUi> = shuttlePassRepository.observeShuttlePass(passId)
        .flatMapLatest { pass ->
            if (pass == null) flowOf(ScannerTripUi()) else routeName(pass.routeId).map { QrScannerUiMapper.toTrip(pass, it) }
        }
        .onStart { emit(ScannerTripUi()) }
        .catch { emit(ScannerTripUi()) }

    val state: StateFlow<QrScannerState> = combine(trip, passengers, input) { trip, passengers, input ->
        QrScannerState(
            trip = trip,
            onBoardCount = passengers.size,
            recentRows = CurrentPassUiMapper.toRows(passengers).take(QrScannerUiMapper.RECENT_ROWS),
            highlightedRowId = (input.result as? ScanResultUi.Boarded)?.passengerId,
            result = input.result,
            camera = input.camera,
            torchOn = input.torchOn && input.camera == CameraAccess.Granted
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QrScannerState())

    /** Boarded here but not yet echoed back by Room, so quick back-to-back scans still number and dedupe correctly. */
    private val pending = mutableListOf<PassengerQR>()
    private var processing = false
    /** The last decoded value; the analyzer reports the same code on many frames while it stays in view. */
    private var recentRaw: String? = null
    private var cooldownJob: Job? = null
    private var resultJob: Job? = null

    fun onAction(action: QrScannerAction) {
        when (action) {
            is QrScannerAction.QrScanned -> onQrScanned(action.rawValue)
            is QrScannerAction.CameraPermissionResult -> input.update {
                it.copy(camera = if (action.granted) CameraAccess.Granted else CameraAccess.Denied)
            }
            QrScannerAction.ToggleTorch -> if (input.value.camera == CameraAccess.Granted) {
                input.update { it.copy(torchOn = !it.torchOn) }
            }
            QrScannerAction.Done, QrScannerAction.Back -> send(QrScannerEvent.NavigateBack)
            QrScannerAction.RequestCamera, QrScannerAction.OpenAppSettings -> Unit
        }
    }

    private fun onQrScanned(raw: String) {
        if (raw.isBlank() || processing || raw == recentRaw) return
        processing = true
        recentRaw = raw
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            delay(REPEAT_COOLDOWN_MS)
            recentRaw = null
        }
        viewModelScope.launch {
            val onBoard = knownPassengers()
            val result = boardPassenger(passId, raw, onBoard)
            if (result is BoardingResult.Boarded) pending += result.passenger
            processing = false

            QrScannerUiMapper.toResult(result)?.let(::showResult)
            when (result) {
                is BoardingResult.Boarded -> send(QrScannerEvent.Feedback(ScanFeedback.Success))
                is BoardingResult.AlreadyOnBoard -> send(QrScannerEvent.Feedback(ScanFeedback.Warning))
                BoardingResult.NotPassenger -> send(QrScannerEvent.Feedback(ScanFeedback.Error))
                is BoardingResult.Failed -> {
                    send(QrScannerEvent.Feedback(ScanFeedback.Error))
                    send(QrScannerEvent.ShowMessage(result.message))
                }
            }
        }
    }

    private fun knownPassengers(): List<PassengerQR> {
        val saved = passengers.value
        pending.removeAll { p -> saved.any { it.id == p.id } }
        return saved + pending
    }

    private fun showResult(result: ScanResultUi) {
        input.update { it.copy(result = result) }
        resultJob?.cancel()
        resultJob = viewModelScope.launch {
            delay(RESULT_VISIBLE_MS)
            input.update { it.copy(result = null) }
        }
    }

    /** A name lookup that always emits, so a missing route shows blank instead of stalling the screen. */
    private fun routeName(routeId: String): Flow<String> = flow { emitAll(routesRepository.getNameByRouteId(routeId)) }
        .catch { emit("") }

    private fun send(event: QrScannerEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private data class Input(
        val result: ScanResultUi? = null,
        val camera: CameraAccess = CameraAccess.Unknown,
        val torchOn: Boolean = false
    )

    companion object {
        const val REPEAT_COOLDOWN_MS = 3_000L
        const val RESULT_VISIBLE_MS = 2_500L
    }
}

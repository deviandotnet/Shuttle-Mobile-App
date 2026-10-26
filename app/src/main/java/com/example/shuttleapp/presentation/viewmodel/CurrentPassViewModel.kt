package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.domain.repository.ShuttleProviderRepository
import com.example.shuttleapp.domain.repository.ShuttleRepository
import com.example.shuttleapp.domain.repository.UserDataRepository
import com.example.shuttleapp.domain.usecase.manualaddpassenger.ValidateQRCodeInput
import com.example.shuttleapp.domain.usecase.shuttlepass.CompleteShuttlePassUseCase
import com.example.shuttleapp.domain.usecase.shuttlepass.CreateDraftPassUseCase
import com.example.shuttleapp.domain.usecase.shuttlepass.CreatePassInput
import com.example.shuttleapp.domain.usecase.shuttlepass.CreatePassResult
import com.example.shuttleapp.presentation.events.CurrentPassAction
import com.example.shuttleapp.presentation.events.CurrentPassEvent
import com.example.shuttleapp.presentation.mapper.CurrentPassUiMapper
import com.example.shuttleapp.presentation.mapper.PassNames
import com.example.shuttleapp.presentation.state.CreatePassFormUi
import com.example.shuttleapp.presentation.state.CurrentPassMode
import com.example.shuttleapp.presentation.state.CurrentPassState
import com.example.shuttleapp.presentation.state.EditPassengerUi
import com.example.shuttleapp.util.Resource
import com.example.shuttleapp.util.displayTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The driver's working pass: the create form while there is no draft, otherwise the open draft
 * with its manifest. Route argument "id" is the signed-in driver's account id.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CurrentPassViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shuttlePassRepository: ShuttlePassRepository,
    private val passengerQRRepository: PassengerQRRepository,
    private val routesRepository: RoutesRepository,
    private val shuttleRepository: ShuttleRepository,
    private val userDataRepository: UserDataRepository,
    private val shuttleProviderRepository: ShuttleProviderRepository,
    private val validateQRCodeInput: ValidateQRCodeInput,
    private val createDraftPass: CreateDraftPassUseCase,
    private val completeShuttlePass: CompleteShuttlePassUseCase
) : ViewModel() {

    private val driverId: String = savedStateHandle["id"] ?: ""
    private var providerId: String = ""
    private var shuttlesJob: Job? = null

    private val _events = Channel<CurrentPassEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** What the driver is doing on screen; everything else is derived from Room. */
    private val input = MutableStateFlow(Input())
    private val routes = MutableStateFlow<List<String>>(emptyList())
    private val plateNumbers = MutableStateFlow<List<String>>(emptyList())

    private val draft: StateFlow<DraftLoad> = shuttlePassRepository.observeDraft(driverId)
        .flatMapLatest { pass -> if (pass == null) flowOf(null) else snapshotOf(pass) }
        .map<Snapshot?, DraftLoad> { DraftLoad.Loaded(it) }
        .catch { ex ->
            send(CurrentPassEvent.ShowMessage("Couldn't load the current pass. ${ex.message.orEmpty()}".trim()))
            emit(DraftLoad.Loaded(null))
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DraftLoad.Loading)

    val state: StateFlow<CurrentPassState> = combine(draft, input, routes, plateNumbers) { draft, input, routes, plates ->
        CurrentPassState(
            mode = when (draft) {
                DraftLoad.Loading -> CurrentPassMode.Loading
                is DraftLoad.Loaded -> draft.snapshot?.let { s ->
                    CurrentPassMode.Active(
                        passId = s.pass.id,
                        card = CurrentPassUiMapper.toCard(s.pass, s.names),
                        passengers = CurrentPassUiMapper.toRows(s.passengers),
                        action = CurrentPassUiMapper.toAction(s.pass, s.passengers.size)
                    )
                } ?: CurrentPassMode.Create
            },
            form = CurrentPassUiMapper.withPreview(input.form.copy(routes = routes, plateNumbers = plates)),
            editingPassenger = input.editing,
            showDeleteConfirm = input.showDeleteConfirm,
            isSubmitting = input.isSubmitting
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CurrentPassState())

    init {
        loadDriver()
        loadRoutes()
    }

    fun onAction(action: CurrentPassAction) {
        when (action) {
            is CurrentPassAction.RouteChanged ->
                updateForm { it.copy(route = action.route, routeError = null) }
            is CurrentPassAction.TripTypeSelected ->
                updateForm { it.copy(tripType = action.tripType, tripTypeError = null) }
            is CurrentPassAction.PlateNumberChanged ->
                updateForm { it.copy(plateNumber = action.plateNumber, plateNumberError = null) }
            CurrentPassAction.CreatePass -> createPass()

            CurrentPassAction.AddPassenger -> current()?.let { send(CurrentPassEvent.NavigateToScanner(it.pass.id)) }
            CurrentPassAction.EditPass -> current()?.let { send(CurrentPassEvent.NavigateToEditPass(it.pass.id)) }
            CurrentPassAction.RequestDeletePass -> input.update { it.copy(showDeleteConfirm = true) }
            CurrentPassAction.DismissDeletePass -> input.update { it.copy(showDeleteConfirm = false) }
            CurrentPassAction.ConfirmDeletePass -> deletePass()
            CurrentPassAction.StartDeparture -> startDeparture()
            CurrentPassAction.CompletePass -> completePass()

            is CurrentPassAction.EditPassenger -> passenger(action.passengerId)?.let { p ->
                input.update { it.copy(editing = EditPassengerUi(p.id, p.scannedQR)) }
            }
            is CurrentPassAction.EditedQrChanged ->
                input.update { it.copy(editing = it.editing?.copy(qrCode = action.qrCode, error = null)) }
            CurrentPassAction.SaveEditedPassenger -> saveEditedPassenger()
            CurrentPassAction.CancelEditPassenger -> input.update { it.copy(editing = null) }
            is CurrentPassAction.DeletePassenger -> deletePassenger(action.passengerId)

            CurrentPassAction.Back -> send(CurrentPassEvent.NavigateBack())
        }
    }

    // --- Loading ---

    private fun loadDriver() = viewModelScope.launch {
        userDataRepository.getUserById(driverId)
            .catch { send(CurrentPassEvent.ShowMessage("Couldn't load your profile.")) }
            .collect { result ->
                when (result) {
                    is Resource.Success -> result.data?.providerId?.takeIf { it != providerId }?.let { id ->
                        providerId = id
                        loadShuttles(id)
                    }
                    is Resource.Error -> send(CurrentPassEvent.ShowMessage(result.message ?: "Couldn't load your profile."))
                    is Resource.Loading -> Unit
                }
            }
    }

    private fun loadRoutes() = viewModelScope.launch {
        routesRepository.getAllRoutes(forceFetchFromRemote = false)
            .catch { send(CurrentPassEvent.ShowMessage("Couldn't load routes.")) }
            .collect { result ->
                when (result) {
                    is Resource.Success -> routes.value = result.data.orEmpty().map { it.routename }
                    // Cached routes still work offline, so only complain when there are none.
                    is Resource.Error -> if (routes.value.isEmpty()) {
                        send(CurrentPassEvent.ShowMessage(result.message ?: "Couldn't load routes."))
                    }
                    is Resource.Loading -> Unit
                }
            }
    }

    private fun loadShuttles(providerId: String) {
        shuttlesJob?.cancel()
        shuttlesJob = viewModelScope.launch {
            shuttleRepository.getShuttleByProviderId(providerId, forceFetchFromRemote = false)
                .catch { send(CurrentPassEvent.ShowMessage("Couldn't load shuttles.")) }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> plateNumbers.value = result.data.orEmpty().map { it.plateNumber }
                        is Resource.Error -> if (plateNumbers.value.isEmpty()) {
                            send(CurrentPassEvent.ShowMessage(result.message ?: "Couldn't load shuttles."))
                        }
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    private fun snapshotOf(pass: ShuttlePass): Flow<Snapshot> = combine(
        passengerQRRepository.getDraftedPassengers(pass.id),
        nameOf { routesRepository.getNameByRouteId(pass.routeId) },
        nameOf { shuttleRepository.getPlateNumberById(pass.plateNumber) },
        nameOf { shuttleProviderRepository.getProviderNameById(pass.provider) },
        nameOf { userDataRepository.getDriverNameById(pass.driver) }
    ) { passengers, route, plate, provider, driver ->
        Snapshot(pass, passengers, PassNames(route, plate, provider, driver))
    }

    /** A name lookup that always emits, so a missing row shows blank instead of stalling the screen. */
    private fun nameOf(lookup: suspend () -> Flow<String?>): Flow<String> = flow { emitAll(lookup()) }
        .map { it.orEmpty() }
        .onEmpty { emit("") }
        .catch { emit("") }

    // --- Create ---

    private fun createPass() {
        val form = input.value.form
        if (input.value.isSubmitting) return
        if (providerId.isBlank()) {
            send(CurrentPassEvent.ShowMessage("Your profile is still loading. Try again in a moment."))
            return
        }
        input.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val result = createDraftPass(
                CreatePassInput(form.route, form.tripType, form.plateNumber, driverId, providerId),
                routes.value,
                plateNumbers.value
            )
            when (result) {
                is CreatePassResult.Invalid -> updateForm {
                    it.copy(
                        routeError = result.routeError,
                        tripTypeError = result.tripTypeError,
                        plateNumberError = result.plateNumberError
                    )
                }
                // The draft flow switches the screen to the new pass.
                is CreatePassResult.Created -> updateForm { CreatePassFormUi() }
                is CreatePassResult.Failed -> send(CurrentPassEvent.ShowMessage(result.message))
            }
            input.update { it.copy(isSubmitting = false) }
        }
    }

    // --- Open draft ---

    private fun startDeparture() {
        val pass = current()?.pass ?: return
        // The snapshot only shows the departure once Room echoes the write, so guard repeat taps until then.
        if (pass.departure.isNotBlank() || input.value.isSubmitting) return
        input.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val departure = displayTime()
            when (val result = shuttlePassRepository.update(listOf(pass.copy(departure = departure)))) {
                is Resource.Error -> send(CurrentPassEvent.ShowMessage(result.message ?: "Couldn't record the departure."))
                else -> send(CurrentPassEvent.ShowMessage("Departure recorded at $departure"))
            }
            input.update { it.copy(isSubmitting = false) }
        }
    }

    private fun completePass() {
        val snapshot = current() ?: return
        if (snapshot.pass.departure.isBlank() || snapshot.passengers.isEmpty() || input.value.isSubmitting) return
        input.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val result = completeShuttlePass(snapshot.pass, snapshot.passengers)) {
                is Resource.Error -> send(CurrentPassEvent.ShowMessage(result.message ?: "Couldn't complete the shuttle pass."))
                else -> send(CurrentPassEvent.NavigateBack("Shuttle pass completed"))
            }
            input.update { it.copy(isSubmitting = false) }
        }
    }

    private fun deletePass() {
        val pass = current()?.pass ?: return
        input.update { it.copy(showDeleteConfirm = false) }
        viewModelScope.launch {
            when (val result = shuttlePassRepository.delete(pass.id)) {
                is Resource.Error -> send(CurrentPassEvent.ShowMessage(result.message ?: "Couldn't delete the shuttle pass."))
                else -> send(CurrentPassEvent.NavigateBack("Draft deleted"))
            }
        }
    }

    // --- Passengers ---

    private fun saveEditedPassenger() {
        val editing = input.value.editing ?: return
        val snapshot = current() ?: return
        val original = snapshot.passengers.firstOrNull { it.id == editing.passengerId } ?: return
        val others = snapshot.passengers.filterNot { it.id == original.id }
        val validation = validateQRCodeInput.execute(editing.qrCode, others)
        if (!validation.successful) {
            input.update { it.copy(editing = editing.copy(error = validation.errorMessage)) }
            return
        }
        viewModelScope.launch {
            try {
                passengerQRRepository.updatePassenger(original.copy(scannedQR = editing.qrCode))
                input.update { it.copy(editing = null) }
                send(CurrentPassEvent.ShowMessage("Passenger updated"))
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                send(CurrentPassEvent.ShowMessage("Couldn't update the passenger. ${ex.message.orEmpty()}".trim()))
            }
        }
    }

    private fun deletePassenger(passengerId: String) {
        val passenger = passenger(passengerId) ?: return
        viewModelScope.launch {
            try {
                passengerQRRepository.deletePassenger(passenger)
                send(CurrentPassEvent.ShowMessage("Passenger removed"))
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                send(CurrentPassEvent.ShowMessage("Couldn't remove the passenger. ${ex.message.orEmpty()}".trim()))
            }
        }
    }

    // --- Helpers ---

    private fun current(): Snapshot? = (draft.value as? DraftLoad.Loaded)?.snapshot

    private fun passenger(id: String): PassengerQR? = current()?.passengers?.firstOrNull { it.id == id }

    private fun updateForm(transform: (CreatePassFormUi) -> CreatePassFormUi) =
        input.update { it.copy(form = transform(it.form)) }

    private fun send(event: CurrentPassEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private data class Input(
        val form: CreatePassFormUi = CreatePassFormUi(),
        val editing: EditPassengerUi? = null,
        val showDeleteConfirm: Boolean = false,
        val isSubmitting: Boolean = false
    )

    private data class Snapshot(val pass: ShuttlePass, val passengers: List<PassengerQR>, val names: PassNames)

    private sealed interface DraftLoad {
        data object Loading : DraftLoad
        data class Loaded(val snapshot: Snapshot?) : DraftLoad
    }
}

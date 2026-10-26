package com.example.shuttleapp.presentation.viewmodel
import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.usecase.logout.LogoutUseCase
import com.example.shuttleapp.domain.repository.ShuttleProviderRepository
import com.example.shuttleapp.domain.repository.ShuttleRepository
import com.example.shuttleapp.domain.repository.UserDataRepository
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.presentation.state.ShuttlePassListLegacyState
import com.example.shuttleapp.presentation.state.ShuttlePassState
import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.model.SyncResult
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import com.example.shuttleapp.presentation.mapper.ShuttlePassItemUiMapper
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.presentation.events.ShuttlePassInputEvent
import com.example.shuttleapp.presentation.events.ShuttlePassUiEvent
import com.example.shuttleapp.presentation.state.CardState
import com.example.shuttleapp.presentation.state.IdNameState
import com.example.shuttleapp.presentation.state.PassengerListState
import com.example.shuttleapp.presentation.state.ShuttlePassInputState
import com.example.shuttleapp.presentation.state.RouteState
import com.example.shuttleapp.presentation.state.ShuttlePassWithPassengerState
import com.example.shuttleapp.presentation.state.ShuttleState
import com.example.shuttleapp.presentation.state.UserState
import com.example.shuttleapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("LongLogTag")
@HiltViewModel
class ShuttleViewModel @Inject constructor(
    private val shuttlePassRepository: ShuttlePassRepository,
    private val routesRepository: RoutesRepository,
    private val shuttleRepository: ShuttleRepository,
    private val userDataRepository: UserDataRepository,
    private val shuttleProviderRepository: ShuttleProviderRepository,
    private val logoutUseCase: LogoutUseCase,
    private val passengerQRRepository: PassengerQRRepository,
    private val itemUiMapper: ShuttlePassItemUiMapper
) : ViewModel() {

    private val _userState = MutableStateFlow(UserState())
    val userState : StateFlow<UserState> = _userState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ShuttlePassUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _routeState = MutableStateFlow(RouteState())
    val routeState : StateFlow<RouteState> = _routeState.asStateFlow()

    //todo check the
    private val _shuttlePassWithPassengers = MutableStateFlow(ShuttlePassWithPassengerState())
    val shuttlePassWithPassengers: StateFlow<ShuttlePassWithPassengerState> = _shuttlePassWithPassengers.asStateFlow()

    private val _shuttleState = MutableStateFlow(ShuttleState())
    val shuttleState : StateFlow<ShuttleState> = _shuttleState.asStateFlow()
    //TODO collect the emitted list of shuttle available

    private val _id = MutableStateFlow("")
    val id: StateFlow<String> = _id

    private val _routeId = MutableStateFlow("")
    val routeId: StateFlow<String> = _routeId.asStateFlow()

    private val _shuttlePassListState = MutableStateFlow(ShuttlePassListLegacyState())
    val shuttlePassListState: StateFlow<ShuttlePassListLegacyState> = _shuttlePassListState.asStateFlow()

    private val _draftedShuttlePassState = MutableStateFlow(ShuttlePassState())
    val draftedShuttlePassState: StateFlow<ShuttlePassState> get() = _draftedShuttlePassState.asStateFlow()

    private val _passenger = MutableStateFlow(PassengerQR())
    val passenger: StateFlow<PassengerQR> = _passenger.asStateFlow()

    private val _shuttlePassInputState = MutableStateFlow(ShuttlePassInputState())
    val shuttlePassInputState: StateFlow<ShuttlePassInputState> = _shuttlePassInputState.asStateFlow()

    private val _forUpdateShuttlePass = MutableStateFlow(ShuttlePass())
    val forUpdateShuttlePass: StateFlow<ShuttlePass> = _forUpdateShuttlePass.asStateFlow()

    private val _draftedPassengerListState = MutableStateFlow(PassengerListState())
    val draftedPassengerListState: StateFlow<PassengerListState> = _draftedPassengerListState.asStateFlow()

    private val _editedShuttlePassState = MutableStateFlow(ShuttlePass())
    val editedShuttlePassState: StateFlow<ShuttlePass> = _editedShuttlePassState.asStateFlow()


    //when true - all of the data is loaded online and not offline capable.
    // when false - load local when no network, offline capable
    init { getAllRoutes(false)}

    private val _postResult = MutableSharedFlow<Resource<SyncResult>>(0)
    val postResult = _postResult.asSharedFlow()


    fun getUserById(accountId: String) {

        viewModelScope.launch(Dispatchers.IO) {
            _userState.update { it.copy(isLoading = true) }

            try {
                userDataRepository.getUserById(accountId).collect { result ->
                    when(result) {
                        is Resource.Error -> {
                            _userState.update { it.copy(
                                isLoading = false,
                                error = result.message) }
                        }
                        is Resource.Loading -> {
                            _userState.update { it.copy(
                                isLoading = true ) }
                        }
                        is Resource.Success -> {
                            result.data?.let { userData ->
                                _userState.update { it.copy(
                                    isLoading = false,
                                    userData = userData
                                ) }
                            }
                        }
                    }
                }
            }catch (ex:Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun syncPass(id: String) {
        sync { shuttlePassRepository.syncPass(id) }
    }

    private fun sync(request: suspend () -> Resource<SyncResult>) {
        viewModelScope.launch {
            _postResult.emit(Resource.Loading(true))
            _postResult.emit(request()) //success or fail
            _postResult.emit(Resource.Loading(false))
        }
    }

    fun getAllShuttleByProviderId(providerId: String, forceFetchFromRemote: Boolean) {

        viewModelScope.launch(Dispatchers.IO) {
            _shuttleState.update { it.copy(isLoading = true) }

            try {
                shuttleRepository.getShuttleByProviderId(providerId, forceFetchFromRemote).collectLatest { result ->
                    when (result) {
                        is Resource.Error -> _shuttleState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                        is Resource.Loading -> _shuttleState.update {
                            it.copy(isLoading = true)
                        }
                        is Resource.Success -> {
                            result.data?.let { shuttles ->
                                _shuttleState.update {
                                    it.copy(
                                        plateNumbers = shuttles.map { shuttle ->
                                            shuttle.plateNumber },
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e("getAllShuttle", "error occurred: ${ex.message}")
            }
        }
    }
    private fun getAllRoutes(forceFetchFromRemote: Boolean) {

        viewModelScope.launch(Dispatchers.IO) {
            _routeState.update { it.copy(isLoading = true) }

            try {
                routesRepository.getAllRoutes(forceFetchFromRemote).collectLatest { result ->
                    when(result) {
                        is Resource.Error -> {
                            _routeState.update { it.copy(isLoading = false, error = result.message) }
                        }
                        is Resource.Success -> {
                            //make sure the data is not null
                            result.data?.let { routes ->
                                _routeState.update {
                                    it.copy(
                                        routes = routes.map { it -> it.routename },
                                        isLoading = false
                                    )
                                }
                            }
                        }
                        is Resource.Loading -> {
                            _routeState.update { it.copy(isLoading = true) }
                        }
                    }
                }

            } catch (ex: Exception) {
                Log.e("getAllRoutes","An error occurred. Please try again. ${ex.message}")
            }
        }

    }
    fun loadShuttleWithPassenger(driverId: String) {
        _shuttlePassListState.update { it.copy( loading = true) }

        viewModelScope.launch {
            try {
                getAllShuttlePassWithPassenger(driverId).collect { shuttlePassWithPassengers ->
                    val items = itemUiMapper.toUi(shuttlePassWithPassengers)
                    _shuttlePassListState.update {
                        it.copy(
                            loading = false,
                            error = null,
                            list = shuttlePassWithPassengers,
                            items = items
                        )
                    }
                }

            }
            catch(ex : Exception) {
                _shuttlePassListState.update {
                    it.copy(
                        loading = false,
                        error = "Error fetching the list. ${ex.message}"
                    )
                }
            }
        }

    }

    fun getShuttlePassWithPassengersById(shuttlePassId: String) {
        _shuttlePassWithPassengers.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                shuttlePassRepository.observePass(shuttlePassId)
                    .collectLatest { data ->
                        _shuttlePassWithPassengers.update {
                            it.copy(
                                isLoading = false,
                                data = data,
                                error = null
                            )
                        }
                    }
            }
            catch (ex: Exception) {
                _shuttlePassWithPassengers.update {
                    it.copy(
                        error = "Error. ${ex.message}",
                        isLoading = false,
                    )
                }
            }
        }
    }


    fun loadShuttlePassDraft(driverId: String) {
        _draftedShuttlePassState.update { it.copy(isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                getDraftedShuttlePass(driverId).collectLatest { data ->
                    _draftedShuttlePassState.update {
                        it.copy(
                            isLoading = false,
                            data = data,
                            error = if(data == null) "No drafted shuttle pass." else null
                        )
                    }

                }
            }
            catch(ex : Exception) {
                _draftedShuttlePassState.update {
                    it.copy(
                        isLoading = false,
                        error = "error: ${ex.message}"
                    )
                }
            }
        }
    }

    fun loadPassenger(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getPassengerById(id).collectLatest{ passenger ->
                    _passenger.value = passenger
                }
            }
            catch (ex: Exception) {
                Log.e("Error", "An error occurred. Please try again. ${ex.message}")
            }

        }
    }

    fun loadShuttlePassById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getShuttlePassById(id).collectLatest { shuttlePass->
                    _forUpdateShuttlePass.update {
                        it.copy(
                            id = shuttlePass.id,
                            routeId = shuttlePass.routeId,
                            provider = shuttlePass.provider,
                            driver = shuttlePass.driver,
                            plateNumber = shuttlePass.plateNumber,
                            date = shuttlePass.date,
                            dateCreated = shuttlePass.dateCreated,
                            tripType = shuttlePass.tripType,
                            departure = shuttlePass.departure,
                            arrival = shuttlePass.arrival,
                            isSynced = shuttlePass.isSynced,
                            isLateShuttle = shuttlePass.isLateShuttle,
                            isDraft = shuttlePass.isDraft
                        )
                    }
                }
            }catch (ex: Exception) {
                Log.e("LoadShuttlePassById", "an error occured while " +
                        "fetching shuttlepass for edit.\n [${ex.stackTrace}] " )
            }
        }
    }

    fun loadEditedShuttlePass(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val shuttlePass = getShuttlePassById(id).first()
                _editedShuttlePassState.value = shuttlePass

                val routeName = unflowGetRouteById(shuttlePass.routeId)
                val plateNumber = shuttleRepository.getPlateNumberById(shuttlePass.plateNumber).first()
                val providerName = shuttleProviderRepository.getProviderNameById(shuttlePass.provider).first()

                _shuttlePassInputState.update {
                    it.copy(
                        driver = shuttlePass.driver,
                        plateNumber = plateNumber ?: "no data",
                        route = routeName,
                        date = shuttlePass.date,
                        tripType = shuttlePass.tripType,
                        shuttleProvider = providerName ?: "no data",
                        departure = shuttlePass.departure,
                        arrival = shuttlePass.arrival
                    )
                }
            }
            catch (ex: Exception) {
                Log.e("Error", "An error occurred: ${ex.message}")
            }
        }
    }

    fun loadDraftedPassenger(shuttlePassId: String) {

        _draftedPassengerListState.update { it.copy(loading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                getDraftedPassenger(shuttlePassId).collectLatest { data ->
                    _draftedPassengerListState.update {
                        it.copy(
                            loading = false,
                            list = data,
                            error = null
                        )
                    }
                }
            } catch (ex: Exception) {
                _draftedPassengerListState.update {
                    it.copy(
                        loading = false,
                        error = "error: ${ex.message}"
                    )
                }
            }
        }
    }


    fun getAllShuttlePassWithPassenger(driverId: String) : Flow<List<ShuttlePassWithPassenger>> {
        return shuttlePassRepository.observePasses(driverId, PassFilter.All)
    }

    fun getDraftedPassenger(shuttlePassId: String) : Flow<List<PassengerQR>> {
        return passengerQRRepository.getDraftedPassengers(shuttlePassId)
    }

    fun getDraftedShuttlePass(driverId: String) : Flow<ShuttlePass?> {
        return shuttlePassRepository.observeDraft(driverId)
    }

    fun getPassengerById(id: String) : Flow<PassengerQR> {
        return passengerQRRepository.getPassengerById(id)

    }


    fun getShuttlePassById(id: String) : Flow<ShuttlePass> {
        return shuttlePassRepository.observeShuttlePass(id).filterNotNull()
    }

    fun getPassengerId(name: String, shuttlePassId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { // run in background
            val result = passengerQRRepository.getPassengerId(name,shuttlePassId)
            withContext(Dispatchers.IO) {
                onResult(result) //send result to UI
            }
        }
    }


    private suspend fun unflowGetRouteById(id: String) : String {
        return routesRepository.getNameByRouteId(id).first()
    }


    private val _toNameState = MutableStateFlow(IdNameState())
    val toNameState : StateFlow<IdNameState> = _toNameState.asStateFlow()

    fun getRouteNameById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                routesRepository.getNameByRouteId(id).collectLatest { result ->
                    _toNameState.update {
                        it.copy(
                            routeName = result
                        )
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getPlateNumberByShuttleId(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                shuttleRepository.getPlateNumberById(id).collectLatest{ result ->
                    _toNameState.update {
                        it.copy(
                            plateNumber = result ?: "no data"
                        )
                    }
                }
            } catch (ex: Exception) {
                Log.e("ShuttleViewModel-PlateNum", "Unexpected error ${ex.message}")

            }

        }
    }

    fun getProviderNameById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            shuttleProviderRepository.getProviderNameById(id).collectLatest { result->
                _toNameState.update {
                    it.copy(
                        providerName = result ?: "no data"
                    )
                }
            }
        }
    }
    fun getDriverNameById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepository.getDriverNameById(id).collectLatest { result ->
                _toNameState.update {
                    it.copy(
                        driverName = result ?: "no data"
                    )
                }
            }
        }
    }

    private val _routeName = MutableStateFlow("")
    val routeName: StateFlow<String> = _routeName


    fun onUpdateShuttlePass(originalPass: ShuttlePass) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val routeId = routesRepository.getRouteIdByName(_shuttlePassInputState.value.route)
                val plateNumber = shuttleRepository.getShuttleIdByPlateNumber(_shuttlePassInputState.value.plateNumber)

                val updatedShuttlePass = listOf(ShuttlePass(
                    id = originalPass.id,
                    routeId = routeId,
                    provider = _shuttlePassInputState.value.shuttleProvider,
                    driver = originalPass.driver,
                    plateNumber = plateNumber,
                    date = originalPass.date,
                    dateCreated = originalPass.dateCreated,
                    tripType = _shuttlePassInputState.value.tripType,
                    departure = _shuttlePassInputState.value.departure,
                    arrival = _shuttlePassInputState.value.arrival,
                    isSynced = originalPass.isSynced,
                    isLateShuttle = originalPass.isLateShuttle,
                    isDraft = originalPass.isDraft
                ))

                when (val result = shuttlePassRepository.update(updatedShuttlePass)) {
                    is Resource.Error -> _uiEvents.emit(ShuttlePassUiEvent.ShowToast("Failed to save changes: ${result.message}"))
                    else -> {
                        _uiEvents.emit(ShuttlePassUiEvent.ShowToast("Changes saved successfully"))
                        _uiEvents.emit(ShuttlePassUiEvent.NavigateUp)
                    }
                }
            } catch(e: Exception) {
                _uiEvents.emit(ShuttlePassUiEvent.ShowToast("Failed to save changes: ${e.message}"))
            }
        }
    }


    private val _cardState = MutableStateFlow(CardState())
    val cardState : StateFlow<CardState> = _cardState.asStateFlow()

    fun getCurrentRouteNameById(id: String) {
        viewModelScope.launch {
            try {
                routesRepository.getNameByRouteId(id).collectLatest { result ->
                    _cardState.update {
                        it.copy(
                            currentRoute = result
                        )
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    fun countUnsyncedShuttlePass(accountId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            shuttlePassRepository.observeUnsyncedCount(accountId).collectLatest { count ->
                _cardState.update {
                    it.copy(
                        unsyncedShuttleCount = count
                    )
                }
            }
        }
    }

    fun countLateShuttlePass(accountId: String) {
        viewModelScope.launch(Dispatchers.IO) {

            shuttlePassRepository.observeLateCount(accountId).collectLatest { count ->
                Log.i("Count", count.toString())
                _cardState.update {
                    it.copy(
                        lateShuttleCount = count
                    )
                }
            }
        }
    }


    fun onShuttlePassInputEvent(event: ShuttlePassInputEvent) {
        when(event) {

            is ShuttlePassInputEvent.DepartureChanged -> {
                _shuttlePassInputState.value = _shuttlePassInputState.value.copy(departure = event.departure)
            }
            is ShuttlePassInputEvent.DriverChanged -> {
                _shuttlePassInputState.value = _shuttlePassInputState.value.copy(driver = event.driver)
            }
            is ShuttlePassInputEvent.PlateNumberChanged -> {
                _shuttlePassInputState.value = _shuttlePassInputState.value.copy(plateNumber = event.plateNumber)
            }
            is ShuttlePassInputEvent.RouteChanged -> {
                _shuttlePassInputState.value = _shuttlePassInputState.value.copy(route = event.route)
            }
            is ShuttlePassInputEvent.ShuttleProviderChanged -> {
                _shuttlePassInputState.value = _shuttlePassInputState.value.copy(shuttleProvider = event.shuttleProvider)
            }
            is ShuttlePassInputEvent.TripTypeChanged -> {
                _shuttlePassInputState.value = _shuttlePassInputState.value.copy(tripType = event.trip)
            }
            is ShuttlePassInputEvent.DateChanged -> {
                _shuttlePassInputState.value = _shuttlePassInputState.value.copy(date = event.date)
            }
            is ShuttlePassInputEvent.ArrivalChanged -> {
                _shuttlePassInputState.value = _shuttlePassInputState.value.copy(arrival = event.arrival)
            }

        }
    }


    fun resetShuttlePassErrorMessages() {
        _shuttlePassInputState.update {
            it.copy(
                shuttleProviderErrorMessage = null,
                driverErrorMessage = null,
                plateNumberErrorMessage = null,
                routeErrorMessage = null,
                tripTypeErrorMessage = null,
                departureErrorMessage = null,
                arrivalErrorMessage = null,
                dateErrorMessage = null
            )
        }
    }


    //auth
    suspend fun logOut() {
        _userState.value = UserState()
        _shuttlePassInputState.value = ShuttlePassInputState()
        logoutUseCase() // ends the session only; passes stay on the device
    }

    // Deletes synced passes and stale drafts older than one month; returns how many were removed.
    suspend fun purgeExpired(driverId: String) : Int {
        return when (val result = shuttlePassRepository.purgeExpired(driverId)) {
            is Resource.Success -> result.data ?: 0
            else -> {
                Log.e("PurgeExpired", result.message ?: "unknown error")
                0
            }
        }
    }


    sealed class ValidationEvent {
        data object Success : ValidationEvent()
    }
}
package com.example.shuttleapp.presentation.viewmodel
import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.repository.RememberedUserRepository
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
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.domain.usecase.manualaddpassenger.ValidateQRCodeInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateDriverInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidatePlateNumberInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateRouteInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateShuttleProviderInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateTripTypeInput
import com.example.shuttleapp.presentation.events.QRInputEvent
import com.example.shuttleapp.presentation.events.ShuttlePassInputEvent
import com.example.shuttleapp.presentation.events.ShuttlePassUiEvent
import com.example.shuttleapp.presentation.state.CardState
import com.example.shuttleapp.presentation.state.IdNameState
import com.example.shuttleapp.presentation.state.PassengerListState
import com.example.shuttleapp.presentation.state.ShuttlePassInputState
import com.example.shuttleapp.presentation.state.QRInputState
import com.example.shuttleapp.presentation.state.RouteState
import com.example.shuttleapp.presentation.state.ShuttlePassWithPassengerState
import com.example.shuttleapp.presentation.state.ShuttleState
import com.example.shuttleapp.presentation.state.UserState
import com.example.shuttleapp.util.Resource
import com.example.shuttleapp.util.displayDate
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
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@SuppressLint("LongLogTag")
@HiltViewModel
class ShuttleViewModel @Inject constructor(
    private val shuttlePassRepository: ShuttlePassRepository,
    private val routesRepository: RoutesRepository,
    private val shuttleRepository: ShuttleRepository,
    private val userDataRepository: UserDataRepository,
    private val shuttleProviderRepository: ShuttleProviderRepository,
    private val loggedUserRepository: RememberedUserRepository,
    private val passengerQRRepository: PassengerQRRepository,

    private val validateQRCodeInput : ValidateQRCodeInput,
    private val validateDriverInput : ValidateDriverInput,
    private val validatePlateNumberInput : ValidatePlateNumberInput,
    private val validateRouteInput : ValidateRouteInput,
    private val validateShuttleProviderInput : ValidateShuttleProviderInput,
    private val validateTripTypeInput : ValidateTripTypeInput
) : ViewModel() {

    private val _userState = MutableStateFlow(UserState())
    val userState : MutableStateFlow<UserState> get() = _userState

    private val _uiEvents = MutableSharedFlow<ShuttlePassUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _routeState = MutableStateFlow(RouteState())
    val routeState : MutableStateFlow<RouteState> get() = _routeState

    val shuttleProviders: List<String> = listOf(
        "BCTSS", "3R&N", "CHERICH")

    //todo check the
    private val _shuttlePassWithPassengers = MutableStateFlow(ShuttlePassWithPassengerState())
    val shuttlePassWithPassengers: MutableStateFlow<ShuttlePassWithPassengerState> = _shuttlePassWithPassengers

    private val _shuttleState = MutableStateFlow(ShuttleState())
    val shuttleState : MutableStateFlow<ShuttleState> get() = _shuttleState
    //TODO collect the emitted list of shuttle available

    private val _id = MutableStateFlow("")
    val id: StateFlow<String> = _id

    private val _routeId = MutableStateFlow("")
    val routeId: MutableStateFlow<String> get() = _routeId

    private val _shuttlePassListState = MutableStateFlow(ShuttlePassListLegacyState())
    val shuttlePassListState: MutableStateFlow<ShuttlePassListLegacyState> get() = _shuttlePassListState

    private val _draftedShuttlePassState = MutableStateFlow(ShuttlePassState())
    val draftedShuttlePassState: StateFlow<ShuttlePassState> get() = _draftedShuttlePassState.asStateFlow()

    private val _passenger = MutableStateFlow(PassengerQR())
    val passenger: MutableStateFlow<PassengerQR> get() = _passenger

    private val _shuttlePassInputState = MutableStateFlow(ShuttlePassInputState())
    val shuttlePassInputState: MutableStateFlow<ShuttlePassInputState> get() = _shuttlePassInputState

    private val _qrInputState = MutableStateFlow(QRInputState())
    val qrInputState: StateFlow<QRInputState> get() = _qrInputState

    private val _forUpdateShuttlePass = MutableStateFlow(ShuttlePass())
    val forUpdateShuttlePass: MutableStateFlow<ShuttlePass> get() = _forUpdateShuttlePass

    private val _draftedPassengerListState = MutableStateFlow(PassengerListState())
    val draftedPassengerListState: MutableStateFlow<PassengerListState> get() = _draftedPassengerListState

    private val _shuttlePass = MutableStateFlow(
        ShuttlePass(
            id = UUID.randomUUID().toString(),
            routeId = "Buena Vista",
            provider = "BCTSS",
            driver = "Erick Manalo",
            plateNumber = "ABCD 1211",
            date = displayDate(),
            dateCreated = System.currentTimeMillis(),
            tripType = "Incoming",
            departure = "",
            arrival = "",
            isSynced = false,
            isLateShuttle = false,
            isDraft = true
        )
    )
    val shuttlePass: MutableStateFlow<ShuttlePass> get() = _shuttlePass

    private val _editedShuttlePassState = MutableStateFlow(ShuttlePass())
    val editedShuttlePassState: MutableStateFlow<ShuttlePass> get() = _editedShuttlePassState

    fun isLateShuttle(tripType: String): Boolean {
        val currentTime: Calendar = Calendar.getInstance()
        val hour: Int = currentTime.get(Calendar.HOUR)
        val minute: Int = currentTime.get(Calendar.MINUTE)

        // Convert hour to 12-hour format
        val hour12: Int = if (currentTime.get(Calendar.AM_PM) == Calendar.AM) {
            hour
        } else {
            hour - 12
        }

        return when (tripType) {
            "Incoming" -> {
                // Check if current time is 6:55 AM or later
                (hour12 == 6 && minute >= 55) || hour12 > 6
            }
            "Outgoing" -> {
                // Check if current time is 6:55 PM or later
                (hour12 == 6 && minute >= 55 && currentTime.get(Calendar.AM_PM) == Calendar.PM)
                        || (hour12 > 6 && currentTime.get(Calendar.AM_PM) == Calendar.PM)
            }
            else -> false
        }
    }

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
                                userState.update { it.copy(
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
                    _shuttlePassListState.update {
                        it.copy(
                            loading = false,
                            error = null,
                            list = shuttlePassWithPassengers
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



    fun finalizeShuttleDraft(driverId: String, shuttlePass: ShuttlePass, passengers: List<PassengerQR>) {
        viewModelScope.launch {
            val result = shuttlePassRepository.finalizeDraft(driverId, shuttlePass, passengers)
            if (result is Resource.Error) {
                _uiEvents.emit(ShuttlePassUiEvent.ShowToast(result.message ?: "Failed to complete the shuttle pass"))
            }
        }
    }

    // Throws on failure so the caller's existing error handling (toast) still runs.
    suspend fun insertShuttlePass(shuttlePass: ShuttlePass) {
        val result = shuttlePassRepository.insert(shuttlePass)
        if (result is Resource.Error) throw IllegalStateException(result.message)
    }

    fun deletePassenger(passenger: PassengerQR) {
        viewModelScope.launch(Dispatchers.IO) {
            passengerQRRepository.deletePassenger(passenger)
        }
    }

    fun deleteShuttlePassById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            shuttlePassRepository.delete(id)
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

    //get the id of drafted shuttlepass id
    fun getShuttlePassIdDraftByDriverId(driverId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val draftId = shuttlePassRepository.getDraftId(driverId)
            withContext(Dispatchers.Main) {
                onResult(draftId)
            }
        }

    }

    suspend fun unflowGetRouteById(id: String) : String {
        return routesRepository.getNameByRouteId(id).first()
    }

    suspend fun getRouteIdByName(name: String) : String {
        return routesRepository.getRouteIdByName(name)
    }

    suspend fun getShuttleIdByPlateNumber(plateNumber: String) : String {
        return shuttleRepository.getShuttleIdByPlateNumber(plateNumber)
    }

    private val _toNameState = MutableStateFlow(IdNameState())
    val toNameState : MutableStateFlow<IdNameState> get() = _toNameState

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

    fun updatePassenger(passenger: PassengerQR) {
        viewModelScope.launch(Dispatchers.IO) {
            passengerQRRepository.updatePassenger(passenger)
        }
    }

    fun updateShuttlePass(shuttlePass: List<ShuttlePass>) {
        viewModelScope.launch(Dispatchers.IO) {
            shuttlePassRepository.update(shuttlePass)
        }
    }

    fun onUpdateShuttlePass(originalPass: ShuttlePass) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val routeId = getRouteIdByName(_shuttlePassInputState.value.route)
                val plateNumber = getShuttleIdByPlateNumber(_shuttlePassInputState.value.plateNumber)

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
    val cardState : MutableStateFlow<CardState> = _cardState

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

    fun onPassengerInputEvent(event : QRInputEvent) {
        when(event) {
            is QRInputEvent.ScannedQRChanged -> {
                _qrInputState.value = _qrInputState.value.copy(qrCode = event.scannedQR)
            }
            is QRInputEvent.Submit -> {
                submitManualAddPassenger()
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
            is ShuttlePassInputEvent.Submit -> {
                submitShuttlePass()

            }

        }
    }

    fun resetShuttlePassInputState() {
        _shuttlePassInputState.value = ShuttlePassInputState()
    }

    fun resetRouteErrorMessage() {
        _shuttlePassInputState.value.routeErrorMessage = null
    }

    fun resetTripTypeErrorMessage() {
        _shuttlePassInputState.value.tripTypeErrorMessage = null
    }
    fun resetPlateNumberErrorMessage() {
        _shuttlePassInputState.value.plateNumberErrorMessage = null
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

    fun resetPassengerInputState() {
        _qrInputState.value = QRInputState()
    }

    //auth
    suspend fun logOut() {
        _userState.value = UserState()
        _shuttlePassInputState.value = ShuttlePassInputState()
        loggedUserRepository.deleteRememberedUser()
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
    fun submitManualAddPassenger() : Boolean {

        val qrCodeResult = validateQRCodeInput.execute(_qrInputState.value.qrCode, _draftedPassengerListState.value.list) //<<-- here i Will use here

        val hasError = listOf(
            qrCodeResult,

            ).any {!it.successful}

        if(hasError) {
            _qrInputState.value = _qrInputState.value.copy(
                qrCodeErrorMessage = qrCodeResult.errorMessage,
            )
            return false
        }

        _qrInputState.value = _qrInputState.value.copy(
            qrCodeErrorMessage = null,
        )
        return true
    }

    fun validateRouteInput() : Boolean {

        val routeResult = validateRouteInput.execute(_shuttlePassInputState.value.route, routeState.value.routes)
        val hasError = listOf(
            routeResult,
        ).any {!it.successful}

        if(hasError) {
            _shuttlePassInputState.value = _shuttlePassInputState.value.copy(
                routeErrorMessage = routeResult.errorMessage,
            )
            return false
        }
        _shuttlePassInputState.value = _shuttlePassInputState.value.copy(
            routeErrorMessage = null,
        )
        return true
    }

    fun validateTripTypeInput() : Boolean {
        val tripTypeResult = validateTripTypeInput.execute(_shuttlePassInputState.value.tripType)
        val hasError = listOf(
            tripTypeResult
        ).any {!it.successful}

        if(hasError) {
            _shuttlePassInputState.value = _shuttlePassInputState.value.copy(
                tripTypeErrorMessage = tripTypeResult.errorMessage
            )
            return false
        }
        _shuttlePassInputState.value = _shuttlePassInputState.value.copy(
            tripTypeErrorMessage = null
        )
        return true
    }

    fun validatePlateNumberInput() : Boolean {
        val plateNumberResult = validatePlateNumberInput.execute(_shuttlePassInputState.value.plateNumber,_shuttleState.value.plateNumbers)

        val hasError = listOf(
            plateNumberResult,
        ).any {!it.successful}

        if(hasError) {
            _shuttlePassInputState.value = _shuttlePassInputState.value.copy(
                plateNumberErrorMessage = plateNumberResult.errorMessage
            )
            return false
        }
        _shuttlePassInputState.value = _shuttlePassInputState.value.copy(
            plateNumberErrorMessage = null
        )
        return true
    }

    fun submitShuttlePass() : Boolean {

        //val departureResult = validateDepartureInput.execute(_shuttlePassInputState.value.departure)
        val driverResult = validateDriverInput.execute(_shuttlePassInputState.value.driver)
        val plateNumberResult = validatePlateNumberInput.execute(_shuttlePassInputState.value.plateNumber,_shuttleState.value.plateNumbers)
        val routeResult = validateRouteInput.execute(_shuttlePassInputState.value.route, routeState.value.routes)
        val shuttleProviderResult = validateShuttleProviderInput.execute(_shuttlePassInputState.value.shuttleProvider, shuttleProviders)
        val tripTypeResult = validateTripTypeInput.execute(_shuttlePassInputState.value.tripType) //nasa validation class yung pang check nito

        val hasError = listOf(
            //departureResult,
            //driverResult,
            plateNumberResult,
            routeResult,
            //shuttleProviderResult,
            tripTypeResult
        ).any {!it.successful}

        if(hasError) {
            _shuttlePassInputState.value = _shuttlePassInputState.value.copy(
                //departureErrorMessage = departureResult.errorMessage,
                //driverErrorMessage = driverResult.errorMessage,
                plateNumberErrorMessage = plateNumberResult.errorMessage,
                routeErrorMessage = routeResult.errorMessage,
                //shuttleProviderErrorMessage = shuttleProviderResult.errorMessage,
                tripTypeErrorMessage = tripTypeResult.errorMessage
            )
            return false
        }

        return true

    }

    sealed class ValidationEvent {
        data object Success : ValidationEvent()
    }
}
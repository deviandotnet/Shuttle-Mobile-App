package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.domain.repository.UserDataRepository
import com.example.shuttleapp.presentation.state.FilterState
import com.example.shuttleapp.presentation.state.FilterType
import com.example.shuttleapp.presentation.state.UserState
import com.example.shuttleapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Flow
import javax.inject.Inject


@HiltViewModel
class FilterViewModel @Inject constructor(
    private val shuttlePassRepository: ShuttlePassRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> get() = _filterState

    fun loadShuttleWithPassenger(driverId: String) {
        viewModelScope.launch {

            shuttlePassRepository.observePasses(driverId, PassFilter.All).collect {
                _filterState.value = filterState.value.copy(
                    list = it,
                    currentFilter = FilterType.All
                )
            }

        }
    }

    fun loadShuttleWithPassengerFilterLate(driverId: String) {
        viewModelScope.launch {

            shuttlePassRepository.observePasses(driverId, PassFilter.Late).collect {
                _filterState.value = filterState.value.copy(
                    list = it,
                    currentFilter = FilterType.Late
                )
            }

        }
    }

    fun loadShuttleWithPassengerFilterUnsynced(driverId: String) {
        viewModelScope.launch {
            shuttlePassRepository.observePasses(driverId, PassFilter.NotSynced).collect {
                _filterState.value = filterState.value.copy(
                    list = it,
                    currentFilter = FilterType.NotSynced
                )
            }

        }
    }

}
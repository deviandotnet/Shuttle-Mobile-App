package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger

data class FilterState(
    var currentFilter: FilterType = FilterType.All,
    val list: List<ShuttlePassWithPassenger> = emptyList()
)

enum class FilterType {
    All, Late, NotSynced
}
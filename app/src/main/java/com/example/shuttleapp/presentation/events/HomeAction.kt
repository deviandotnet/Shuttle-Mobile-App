package com.example.shuttleapp.presentation.events

import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.presentation.components.home.HomeNavItem

/** Everything the driver can do on the Home dashboard. */
sealed interface HomeAction {
    data object OpenProfile : HomeAction
    /** The FAB: resumes the draft when there is one, otherwise starts a new pass. */
    data object PrimaryAction : HomeAction
    data class OpenPass(val id: String) : HomeAction
    /** "View all" and the stat tiles: the pass list, optionally pre-filtered. */
    data class OpenPassList(val filter: PassFilter = PassFilter.All) : HomeAction
    data object SyncNow : HomeAction
    data object RetryPasses : HomeAction
    data class Navigate(val item: HomeNavItem) : HomeAction
}

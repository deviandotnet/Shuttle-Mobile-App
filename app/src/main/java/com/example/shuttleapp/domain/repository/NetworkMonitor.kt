package com.example.shuttleapp.domain.repository

import kotlinx.coroutines.flow.Flow

/** Whether the device currently has a validated internet connection. Only login and upload need it. */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}

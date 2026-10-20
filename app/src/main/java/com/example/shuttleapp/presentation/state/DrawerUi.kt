package com.example.shuttleapp.presentation.state

/** Display-ready account menu (mockup 04): profile, sync card and version line. */
data class DrawerUi(
    val name: String = "",
    /** "Employee no. 15125112"; blank when the account has none. */
    val employeeNo: String = "",
    /** The driver's shuttle provider; blank until it is known. */
    val providerName: String = "",
    val initials: String = "",
    val pendingCount: Int = 0,
    /** "Last synced today, 10:42". */
    val lastSynced: String = "",
    val isSyncing: Boolean = false,
    /** "Version 1.0 · Offline data saved on this device". */
    val versionLine: String = ""
)

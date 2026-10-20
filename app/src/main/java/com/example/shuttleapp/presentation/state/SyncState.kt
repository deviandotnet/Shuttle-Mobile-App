package com.example.shuttleapp.presentation.state

/** The Sync tab, plus the pending-sync callout on Home and the sync card in the drawer. */
data class SyncState(
    /** Finalized passes not yet uploaded, newest first. */
    val pending: List<ShuttlePassItemUi> = emptyList(),
    val isLoading: Boolean = true,
    /** "Last synced today, 10:42", or a not-yet message. */
    val lastSynced: String = "",
    val upload: PassUploadState = PassUploadState()
) {
    val pendingCount: Int get() = pending.size
    val isSyncing: Boolean get() = upload.sync == SyncStatus.Running
}

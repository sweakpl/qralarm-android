package com.sweak.qralarm.features.backup

sealed class BackupScreenUserEvent {
    data object OnBackClicked : BackupScreenUserEvent()
    data object OnExportBackupClicked : BackupScreenUserEvent()
    data class BackupDestinationPicked(val destinationUriString: String) : BackupScreenUserEvent()
}

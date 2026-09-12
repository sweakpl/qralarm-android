package com.sweak.qralarm.features.backup

sealed class BackupScreenUserEvent {
    data object OnBackClicked : BackupScreenUserEvent()
    data object OnExportBackupClicked : BackupScreenUserEvent()
    data class BackupDestinationPicked(val destinationUriString: String) : BackupScreenUserEvent()
    data object OnImportBackupClicked : BackupScreenUserEvent()
    data class ImportConfirmationDialogVisible(val isVisible: Boolean) : BackupScreenUserEvent()
    data object OnImportBackupConfirmed : BackupScreenUserEvent()
    data class BackupSourcePicked(val sourceUriString: String) : BackupScreenUserEvent()
}

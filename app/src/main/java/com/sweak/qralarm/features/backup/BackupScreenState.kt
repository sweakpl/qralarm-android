package com.sweak.qralarm.features.backup

data class BackupScreenState(
    val isBackupInProgress: Boolean = false,
    val isImportInProgress: Boolean = false,
    val isImportConfirmationDialogVisible: Boolean = false,
    val message: BackupScreenMessage? = null
)

sealed class BackupScreenMessage {
    data object BackupCreated : BackupScreenMessage()
    data object BackupCreationFailed : BackupScreenMessage()
    data object BackupImported : BackupScreenMessage()
    data object BackupNotRecognized : BackupScreenMessage()
    data object BackupTooNew : BackupScreenMessage()
    data object BackupImportFailed : BackupScreenMessage()
}

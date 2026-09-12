package com.sweak.qralarm.features.backup

data class BackupScreenState(
    val isBackupInProgress: Boolean = false,
    val message: BackupScreenMessage? = null
)

sealed class BackupScreenMessage {
    data object BackupCreated : BackupScreenMessage()
    data object BackupCreationFailed : BackupScreenMessage()
}

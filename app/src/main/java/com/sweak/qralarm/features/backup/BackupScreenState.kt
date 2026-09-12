package com.sweak.qralarm.features.backup

import com.sweak.qralarm.core.ui.compose_util.UiText

data class BackupScreenState(
    val isBackupInProgress: Boolean = false,
    val isImportInProgress: Boolean = false,
    val isImportConfirmationDialogVisible: Boolean = false,
    val exportMessage: BackupScreenMessage? = null,
    val importMessage: BackupScreenMessage? = null
)

sealed class BackupScreenMessage {
    abstract val text: UiText

    data class Success(override val text: UiText) : BackupScreenMessage()
    data class Failure(override val text: UiText) : BackupScreenMessage()
}

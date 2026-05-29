package com.sweak.qralarm.features.codes_management

import com.sweak.qralarm.core.ui.model.Code

sealed class CodesManagementScreenUserEvent {
    data object OnBackClicked : CodesManagementScreenUserEvent()
    data object TryScanDefaultCode : CodesManagementScreenUserEvent()
    data object PickExistingCodeAsDefaultClicked : CodesManagementScreenUserEvent()
    data object CancelPickingCodeClicked : CodesManagementScreenUserEvent()
    data class CodePickedAsDefault(val code: Code) : CodesManagementScreenUserEvent()
    data object ClearDefaultCodeClicked : CodesManagementScreenUserEvent()
    data class EditCodeNameClicked(val code: Code) : CodesManagementScreenUserEvent()
    data class CodeNameEditConfirmed(val newName: String?) : CodesManagementScreenUserEvent()
    data object CodeNameEditDismissed : CodesManagementScreenUserEvent()
    data class CameraPermissionDeniedDialogVisible(val isVisible: Boolean) :
        CodesManagementScreenUserEvent()
    data object GoToApplicationSettingsClicked : CodesManagementScreenUserEvent()
}

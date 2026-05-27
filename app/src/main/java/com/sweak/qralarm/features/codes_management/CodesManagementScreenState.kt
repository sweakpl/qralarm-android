package com.sweak.qralarm.features.codes_management

import com.sweak.qralarm.core.ui.model.Code

data class CodesManagementScreenState(
    val isLoading: Boolean = true,
    val defaultAlarmCode: Code? = null,
    val otherCodes: List<Code> = emptyList(),
    val isPickingDefault: Boolean = false,
    val codeBeingEdited: Code? = null,
    val isCameraPermissionDeniedDialogVisible: Boolean = false
)

package com.sweak.qralarm.features.add_edit_alarm

import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import kotlinx.coroutines.flow.StateFlow

interface AlarmDraftHandle {
    val draft: StateFlow<AlarmDraft>
    val isLoading: StateFlow<Boolean>
    val previouslySavedCodes: StateFlow<List<Code>>
    val hasUnsavedChanges: Boolean
    val isRestoredFromSavedState: Boolean

    fun updateDraft(
        countsAsUnsavedChange: Boolean = true,
        transform: (AlarmDraft) -> AlarmDraft
    )
}

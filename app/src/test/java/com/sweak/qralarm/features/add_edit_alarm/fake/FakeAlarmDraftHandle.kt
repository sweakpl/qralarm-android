package com.sweak.qralarm.features.add_edit_alarm.fake

import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.features.add_edit_alarm.AlarmDraftHandle
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeAlarmDraftHandle(initialDraft: AlarmDraft) : AlarmDraftHandle {
    override val draft = MutableStateFlow(initialDraft)
    override val isLoading = MutableStateFlow(false)
    override val previouslySavedCodes = MutableStateFlow(emptyList<Code>())
    override val hasUnsavedChanges = false
    override val isRestoredFromSavedState = false

    override fun updateDraft(
        countsAsUnsavedChange: Boolean,
        transform: (AlarmDraft) -> AlarmDraft
    ) {
        draft.update(transform)
    }
}

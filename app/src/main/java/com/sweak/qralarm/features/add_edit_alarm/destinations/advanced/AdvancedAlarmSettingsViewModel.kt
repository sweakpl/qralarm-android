package com.sweak.qralarm.features.add_edit_alarm.destinations.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.features.add_edit_alarm.AlarmDraftHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel(assistedFactory = AdvancedAlarmSettingsViewModel.Factory::class)
class AdvancedAlarmSettingsViewModel @AssistedInject constructor(
    @Assisted private val alarmDraftHandle: AlarmDraftHandle
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(alarmDraftHandle: AlarmDraftHandle): AdvancedAlarmSettingsViewModel
    }

    private val uiState = MutableStateFlow(AdvancedAlarmSettingsUiState())

    val state: StateFlow<AdvancedAlarmSettingsState> =
        combine(alarmDraftHandle.draft, uiState) { draft, uiState ->
            AdvancedAlarmSettingsState(draft = draft, ui = uiState)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AdvancedAlarmSettingsState(
                draft = alarmDraftHandle.draft.value,
                ui = uiState.value
            )
        )

    fun onEvent(event: AdvancedAlarmSettingsScreenUserEvent) {
        when (event) {
            is AdvancedAlarmSettingsScreenUserEvent.OnCancelClicked -> Unit

            is AdvancedAlarmSettingsScreenUserEvent.ChooseGentleWakeUpDurationDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseGentleWakeUpDurationDialogVisible = event.isVisible)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.GentleWakeUpDurationSelected -> {
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(
                        gentleWakeupDurationInSeconds = event.newGentleWakeUpDurationInSeconds
                    )
                }

                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseGentleWakeUpDurationDialogVisible = false)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.ChooseTemporaryMuteDurationDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(
                        isChooseTemporaryMuteDurationDialogVisible = event.isVisible
                    )
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.TemporaryMuteDurationSelected -> {
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(
                        temporaryMuteDurationInSeconds = event.newTemporaryMuteDurationInSeconds
                    )
                }

                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseTemporaryMuteDurationDialogVisible = false)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.OpenCodeLinkEnabledChanged -> {
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(isOpenCodeLinkEnabled = event.isEnabled)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.ChooseCancelLockDurationDialogVisible -> {
                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseCancelLockDurationDialogVisible = event.isVisible)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.CancelLockDurationSelected -> {
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(cancelLockDurationInMinutes = event.newCancelLockDurationInMinutes)
                }

                uiState.update { currentUiState ->
                    currentUiState.copy(isChooseCancelLockDurationDialogVisible = false)
                }
            }

            is AdvancedAlarmSettingsScreenUserEvent.EmergencyTaskEnabledChanged -> {
                alarmDraftHandle.updateDraft { draft ->
                    draft.copy(isEmergencyTaskEnabled = event.isEnabled)
                }
            }
        }
    }
}

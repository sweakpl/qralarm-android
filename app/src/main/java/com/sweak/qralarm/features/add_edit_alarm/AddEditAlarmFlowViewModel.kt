package com.sweak.qralarm.features.add_edit_alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.ui.convertAlarmRepeatingMode
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmDraftMapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AddEditAlarmFlowViewModel.Factory::class)
class AddEditAlarmFlowViewModel @AssistedInject constructor(
    @Assisted private val idOfAlarmToEdit: Long,
    private val savedStateHandle: SavedStateHandle,
    private val alarmRingtonePlayer: AlarmRingtonePlayer,
    private val alarmsRepository: AlarmsRepository,
    private val codesRepository: CodesRepository,
    private val alarmDraftMapper: AlarmDraftMapper
) : ViewModel(), AlarmDraftHandle {

    @AssistedFactory
    interface Factory {
        fun create(idOfAlarmToEdit: Long): AddEditAlarmFlowViewModel
    }

    private val _draft = savedStateHandle.getMutableStateFlow(DRAFT_KEY, AlarmDraft())
    override val draft: StateFlow<AlarmDraft> = _draft.asStateFlow()

    private val _isLoading = savedStateHandle.getMutableStateFlow(IS_LOADING_KEY, true)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _previouslySavedCodes =
        savedStateHandle.getMutableStateFlow(PREVIOUSLY_SAVED_CODES_KEY, emptyList<Code>())
    override val previouslySavedCodes: StateFlow<List<Code>> = _previouslySavedCodes.asStateFlow()

    override var hasUnsavedChanges: Boolean
        get() = savedStateHandle[HAS_UNSAVED_CHANGES_KEY] ?: false
        private set(value) {
            savedStateHandle[HAS_UNSAVED_CHANGES_KEY] = value
        }

    override val isRestoredFromSavedState = !_isLoading.value

    init {
        if (!isRestoredFromSavedState) {
            viewModelScope.launch { loadDraft() }
        }
    }

    override fun updateDraft(
        countsAsUnsavedChange: Boolean,
        transform: (AlarmDraft) -> AlarmDraft
    ) {
        var hasDraftChanged = false

        _draft.update { currentDraft ->
            val newDraft = transform(currentDraft)

            hasDraftChanged = newDraft != currentDraft
            newDraft
        }

        if (countsAsUnsavedChange && hasDraftChanged) {
            hasUnsavedChanges = true
        }
    }

    private suspend fun loadDraft() {
        val allSavedAlarmCodes = codesRepository.getCodesFlow()
            .first()
            .map { Code(id = it.codeId, value = it.value, name = it.name) }

        val loadDraft: (AlarmDraft) -> AlarmDraft

        if (idOfAlarmToEdit == 0L) {
            loadDraft = { currentDraft ->
                alarmDraftMapper.withNewAlarm(currentDraft = currentDraft)
            }
        } else {
            val alarm = alarmsRepository.getAlarm(alarmId = idOfAlarmToEdit) ?: return
            val alarmRepeatingScheduleWrapper = convertAlarmRepeatingMode(
                repeatingMode = alarm.repeatingMode
            ) ?: return
            val isSystemDefaultAlarmRingtoneAvailable =
                alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable()

            loadDraft = { currentDraft ->
                alarmDraftMapper.withExistingAlarm(
                    currentDraft = currentDraft,
                    alarm = alarm,
                    alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                    isSystemDefaultAlarmRingtoneAvailable = isSystemDefaultAlarmRingtoneAvailable
                )
            }
        }

        _draft.update(loadDraft)
        _previouslySavedCodes.value = allSavedAlarmCodes
        _isLoading.value = false
    }

    companion object {
        private const val DRAFT_KEY = "draft"
        private const val IS_LOADING_KEY = "isLoading"
        private const val PREVIOUSLY_SAVED_CODES_KEY = "previouslySavedCodes"
        private const val HAS_UNSAVED_CHANGES_KEY = "hasUnsavedChanges"
    }
}

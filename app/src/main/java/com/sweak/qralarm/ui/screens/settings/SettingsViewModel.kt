package com.sweak.qralarm.ui.screens.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    val settingsUiState: MutableState<SettingsUiState> = runBlocking {
        dataStoreManager.let {
            mutableStateOf(
                SettingsUiState(
                    availableAlarmSounds = AVAILABLE_ALARM_SOUNDS.map { alarmSound ->
                        resourceProvider.getString(alarmSound.nameResourceId)
                    },
                    selectedAlarmSoundIndex = AVAILABLE_ALARM_SOUNDS.indexOf(
                        AlarmSound.fromInt(it.getInt(DataStoreManager.ALARM_SOUND).first())
                    ),
                    selectedSnoozeDurationIndex = AVAILABLE_SNOOZE_DURATIONS.indexOf(
                        it.getInt(DataStoreManager.SNOOZE_DURATION_MINUTES).first()
                    ),
                    selectedSnoozeMaxCountIndex = AVAILABLE_SNOOZE_MAX_COUNTS.indexOf(
                        it.getInt(DataStoreManager.SNOOZE_MAX_COUNT).first()
                    )
                )
            )
        }
    }

    fun updateAlarmSoundSelection(newIndex: Int) {
        val newSelectedAlarmSound = AVAILABLE_ALARM_SOUNDS[newIndex]

        settingsUiState.value = settingsUiState.value.copy(
            selectedAlarmSoundIndex = newIndex
        )

        viewModelScope.launch {
            dataStoreManager.putInt(
                DataStoreManager.ALARM_SOUND,
                newSelectedAlarmSound.ordinal
            )
        }
    }

    fun updateSnoozeDurationSelection(newIndex: Int) {
        val newSelectedSnoozeDuration = AVAILABLE_SNOOZE_DURATIONS[newIndex]

        settingsUiState.value = settingsUiState.value.copy(
            selectedSnoozeDurationIndex = newIndex
        )

        viewModelScope.launch {
            dataStoreManager.putInt(
                DataStoreManager.SNOOZE_DURATION_MINUTES,
                newSelectedSnoozeDuration
            )
        }
    }

    fun updateSnoozeMaxCountSelection(newIndex: Int) {
        val newSelectedSnoozeMaxCount = AVAILABLE_SNOOZE_MAX_COUNTS[newIndex]

        settingsUiState.value = settingsUiState.value.copy(
            selectedSnoozeMaxCountIndex = newIndex
        )

        viewModelScope.launch {
            dataStoreManager.putInt(
                DataStoreManager.SNOOZE_MAX_COUNT,
                newSelectedSnoozeMaxCount
            )
        }
    }
}
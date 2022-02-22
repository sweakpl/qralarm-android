package com.sweak.qralarm.ui.screens.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.AVAILABLE_SNOOZE_DURATIONS
import com.sweak.qralarm.util.AVAILABLE_SNOOZE_MAX_COUNTS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val settingsUiState: MutableState<SettingsUiState> = runBlocking {
        dataStoreManager.let {
            mutableStateOf(
                SettingsUiState(
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

    fun updateSnoozeDurationSelection(newIndex: Int) {
        val newSelectedSnoozeDuration = settingsUiState.value.availableSnoozeDurations[newIndex]

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
        val newSelectedSnoozeMaxCount = settingsUiState.value.availableSnoozeMaxCounts[newIndex]

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
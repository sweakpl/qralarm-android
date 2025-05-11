package com.sweak.qralarm.features.custom_code_scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.features.custom_code_scanner.navigation.SHOULD_SCAN_FOR_DEFAULT_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomCodeScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository
): ViewModel() {

    private val shouldScanForDefaultCode =
        savedStateHandle.get<Boolean>(SHOULD_SCAN_FOR_DEFAULT_CODE) == true

    private val backendEventsChannel = Channel<CustomCodeScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    fun onEvent(event: CustomCodeScannerScreenUserEvent) {
        when (event) {
            is CustomCodeScannerScreenUserEvent.CodeResultScanned -> viewModelScope.launch {
                if (shouldScanForDefaultCode) {
                    userDataRepository.setDefaultAlarmCode(code = event.result.text)
                } else {
                    userDataRepository.setTemporaryScannedCode(code = event.result.text)
                }

                backendEventsChannel.send(CustomCodeScannerScreenBackendEvent.CustomCodeSaved)
            }
            else -> { /* no-op */ }
        }
    }
}
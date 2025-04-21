package com.sweak.qralarm.features.custom_code_scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomCodeScannerViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
): ViewModel() {

    private val backendEventsChannel = Channel<CustomCodeScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    fun onEvent(event: CustomCodeScannerScreenUserEvent) {
        when (event) {
            is CustomCodeScannerScreenUserEvent.CodeResultScanned -> viewModelScope.launch {
                userDataRepository.setTemporaryScannedCode(code = event.result.text)
                backendEventsChannel.send(CustomCodeScannerScreenBackendEvent.CustomCodeSaved)
            }
            else -> { /* no-op */ }
        }
    }
}
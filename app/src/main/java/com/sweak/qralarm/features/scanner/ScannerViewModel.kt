package com.sweak.qralarm.features.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
): ViewModel() {

    private val backendEventsChannel = Channel<ScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    fun onEvent(event: ScannerScreenUserEvent) {
        when (event) {
            is ScannerScreenUserEvent.CodeResultScanned -> viewModelScope.launch {
                userDataRepository.setTemporaryScannedCode(code = event.result.text)
                backendEventsChannel.send(ScannerScreenBackendEvent.CustomCodeSaved)
            }
        }
    }
}
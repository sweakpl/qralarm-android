package com.sweak.qralarm.features.rate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class RateViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private var _state = MutableStateFlow(RateScreenState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<RateScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            _state.update { currentState ->
                val nextRatePromptTimeInMillis =
                    userDataRepository.nextRatePromptTimeInMillis.first()

                currentState.copy(isNeverShowAgainChecked = nextRatePromptTimeInMillis == 0L)
            }
        }
    }

    fun onEvent(event: RateScreenUserEvent) {
        when (event) {
            is RateScreenUserEvent.IsNeverShowAgainCheckedChanged -> viewModelScope.launch {
                userDataRepository.setNextRatePromptTimeInMillis(promptTime = 0L)
                _state.update { currentState ->
                    currentState.copy(isNeverShowAgainChecked = event.checked)
                }
            }
            is RateScreenUserEvent.RateMeClicked -> viewModelScope.launch {
                userDataRepository.setNextRatePromptTimeInMillis(promptTime = 0L)
                backendEventsChannel.send(RateScreenBackendEvent.RateMeClickProcessed)
            }
            is RateScreenUserEvent.SomethingWrongClicked -> viewModelScope.launch {
                userDataRepository.setNextRatePromptTimeInMillis(promptTime = 0L)
                backendEventsChannel.send(RateScreenBackendEvent.SomethingWrongClickProcessed)
            }
            is RateScreenUserEvent.NotNowClicked -> viewModelScope.launch {
                if (!state.value.isNeverShowAgainChecked) {
                    userDataRepository.setNextRatePromptTimeInMillis(
                        promptTime = ZonedDateTime.now().plusMonths(1).toInstant().toEpochMilli()
                    )
                }

                backendEventsChannel.send(RateScreenBackendEvent.NotNowClickProcessed)
            }
        }
    }
}
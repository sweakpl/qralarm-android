package com.sweak.qralarm.features.rate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
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

    var state = MutableStateFlow(RateScreenState())

    private val backendEventsChannel = Channel<RateScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            state.update { currentState ->
                val nextRatePromptTimeInMillis =
                    userDataRepository.nextRatePromptTimeInMillis.first()

                currentState.copy(
                    isNeverShowAgainChecked =
                    nextRatePromptTimeInMillis == null || nextRatePromptTimeInMillis == 0L
                )
            }
        }
    }

    fun onEvent(event: RateScreenUserEvent) {
        when (event) {
            is RateScreenUserEvent.IsNeverShowAgainCheckedChanged -> {
                state.update { currentState ->
                    currentState.copy(isNeverShowAgainChecked = event.checked)
                }
            }
            is RateScreenUserEvent.RateMeClicked -> viewModelScope.launch {
                userDataRepository.setNextRatePromptTimeInMillis(promptTime = null)
                backendEventsChannel.send(RateScreenBackendEvent.RateMeClickProcessed)
            }
            is RateScreenUserEvent.SomethingWrongClicked -> viewModelScope.launch {
                userDataRepository.setNextRatePromptTimeInMillis(promptTime = null)
                backendEventsChannel.send(RateScreenBackendEvent.SomethingWrongClickProcessed)
            }
            is RateScreenUserEvent.NotNowClicked -> viewModelScope.launch {
                userDataRepository.setNextRatePromptTimeInMillis(
                    promptTime =  if (!state.value.isNeverShowAgainChecked) {
                        ZonedDateTime.now().plusMonths(1).toInstant().toEpochMilli()
                    } else{
                        null
                    }
                )

                backendEventsChannel.send(RateScreenBackendEvent.NotNowClickProcessed)
            }
        }
    }
}
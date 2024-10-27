package com.sweak.qralarm.features.introduction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroductionViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
): ViewModel() {

    private val backendEventsChannel = Channel<IntroductionScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    fun onEvent(event: IntroductionScreenUserEvent) {
        when (event) {
            IntroductionScreenUserEvent.ContinueClicked -> viewModelScope.launch {
                userDataRepository.setIntroductionFinished(finished = true)

                backendEventsChannel.send(
                    IntroductionScreenBackendEvent.IntroductionFinishConfirmed
                )
            }
        }
    }
}
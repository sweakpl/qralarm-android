package com.sweak.qralarm.features.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.Theme
import com.sweak.qralarm.features.theme.model.ThemeUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ThemeScreenState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<ThemeScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            userDataRepository.theme.collect { theme ->
                _state.update { currentTheme ->
                    currentTheme.copy(
                        theme = when (theme) {
                            is Theme.Default -> ThemeUi.Default
                            is Theme.Dynamic -> ThemeUi.Dynamic
                        }
                    )
                }
            }
        }
    }

    fun onEvent(event: ThemeScreenUserEvent) {
        when (event) {
            is ThemeScreenUserEvent.OnDynamicThemeToggled -> viewModelScope.launch {
                val dynamicThemeEnabled = event.isChecked

                userDataRepository.setTheme(
                    theme = if (dynamicThemeEnabled) Theme.Dynamic else Theme.Default
                )

                _state.update { currentState ->
                    currentState.copy(
                        theme = if (dynamicThemeEnabled) ThemeUi.Dynamic else ThemeUi.Default
                    )
                }
            }
            is ThemeScreenUserEvent.OnCustomThemeToggled -> viewModelScope.launch {
                backendEventsChannel.send(ThemeScreenBackendEvent.RedirectToQRAlarmPro)
            }
            is ThemeScreenUserEvent.OnCustomThemeSelected -> viewModelScope.launch {
                backendEventsChannel.send(ThemeScreenBackendEvent.RedirectToQRAlarmPro)
            }
            is ThemeScreenUserEvent.OnColorPickerOpened -> viewModelScope.launch {
                backendEventsChannel.send(ThemeScreenBackendEvent.RedirectToQRAlarmPro)
            }
            else -> { /* no-op */ }
        }
    }
}

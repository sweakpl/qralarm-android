package com.sweak.qralarm.features.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MenuScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userDataRepository.theme.collect { theme ->
                _state.update { currentState ->
                    currentState.copy(theme = theme)
                }
            }
        }
    }

    fun onEvent(event: MenuScreenUserEvent) {
        when (event) {
            else -> { /* no-op */ }
        }
    }
}

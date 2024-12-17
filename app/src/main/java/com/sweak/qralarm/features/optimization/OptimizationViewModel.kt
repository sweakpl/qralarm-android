package com.sweak.qralarm.features.optimization

import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.features.optimization.navigation.IS_LAUNCHED_FROM_MENU
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class OptimizationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val powerManager: PowerManager,
    @Named("PackageName") private val packageName: String,
    private val userDataRepository: UserDataRepository
): ViewModel() {

    var state = MutableStateFlow(OptimizationScreenState())

    init {
        state.update { currentState ->
            currentState.copy(
                shouldDelayInstructionsTransitions =
                !(savedStateHandle[IS_LAUNCHED_FROM_MENU] ?: true)
            )
        }

        viewModelScope.launch {
            val optimizationGuideState = userDataRepository.optimizationGuideState.first()

            if (optimizationGuideState == UserDataRepository.OptimizationGuideState.SHOULD_BE_SEEN) {
                userDataRepository.setOptimizationGuideState(
                    state = UserDataRepository.OptimizationGuideState.HAS_BEEN_SEEN
                )
            }

            refreshInternal()
        }
    }

    fun refresh() = viewModelScope.launch {
        // This delay is supposed to ensure that
        // powerManager.isIgnoringBatteryOptimizations returns an updated value - it can
        // take some time until it is updated on some systems.
        delay(1000)
        refreshInternal()
    }

    private fun refreshInternal() {
        state.update { currentState ->
            currentState.copy(
                isIgnoringBatteryOptimizations =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    powerManager.isIgnoringBatteryOptimizations(packageName)
                } else true
            )
        }
    }
}
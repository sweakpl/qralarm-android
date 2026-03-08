package com.sweak.qralarm.features.optimization

import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.OptimizationGuideState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Named

@HiltViewModel(assistedFactory = OptimizationViewModel.Factory::class)
class OptimizationViewModel @AssistedInject constructor(
    @Assisted isLaunchedFromMenu: Boolean,
    private val powerManager: PowerManager,
    @param:Named("PackageName") private val packageName: String,
    private val userDataRepository: UserDataRepository
): ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(isLaunchedFromMenu: Boolean): OptimizationViewModel
    }

    private var _state = MutableStateFlow(OptimizationScreenState())
    val state = _state.asStateFlow()

    init {
        _state.update { currentState ->
            currentState.copy(
                shouldDelayInstructionsTransitions = !isLaunchedFromMenu
            )
        }

        viewModelScope.launch {
            val optimizationGuideState = userDataRepository.optimizationGuideState.first()

            if (optimizationGuideState == OptimizationGuideState.SHOULD_BE_SEEN) {
                userDataRepository.setOptimizationGuideState(
                    state = OptimizationGuideState.HAS_BEEN_SEEN
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
        _state.update { currentState ->
            currentState.copy(
                isIgnoringBatteryOptimizations =
                    powerManager.isIgnoringBatteryOptimizations(packageName)
            )
        }
    }
}
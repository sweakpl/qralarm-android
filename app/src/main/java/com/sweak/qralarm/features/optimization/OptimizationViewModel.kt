package com.sweak.qralarm.features.optimization

import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class OptimizationViewModel @Inject constructor(
    private val powerManager: PowerManager,
    @Named("PackageName") private val packageName: String
): ViewModel() {

    var state = MutableStateFlow(OptimizationScreenState())

    init {
        refreshInternal()
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
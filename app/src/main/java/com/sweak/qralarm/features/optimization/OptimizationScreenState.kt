package com.sweak.qralarm.features.optimization

data class OptimizationScreenState(
    val isIgnoringBatteryOptimizations: Boolean = false,
    val shouldDelayInstructionsTransitions: Boolean = false
)

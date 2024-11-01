package com.sweak.qralarm.features.optimization

sealed class OptimizationScreenUserEvent {
    data object OnBackClicked : OptimizationScreenUserEvent()
    data object EnableBackgroundWork : OptimizationScreenUserEvent()
    data object BackgroundWorkWebsiteClicked : OptimizationScreenUserEvent()
    data object ApplicationSettingsClicked : OptimizationScreenUserEvent()
}
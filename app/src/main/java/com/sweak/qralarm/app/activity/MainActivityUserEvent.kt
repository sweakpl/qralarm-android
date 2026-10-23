package com.sweak.qralarm.app.activity

sealed class MainActivityUserEvent {
    data object OnOnboardingFinished : MainActivityUserEvent()
    data object OnResumed : MainActivityUserEvent()
    data object OnPaused : MainActivityUserEvent()
    data object OnWhatsNewDialogDismissed : MainActivityUserEvent()
}
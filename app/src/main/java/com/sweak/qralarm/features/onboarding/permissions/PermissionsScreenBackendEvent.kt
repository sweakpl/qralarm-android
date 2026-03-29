package com.sweak.qralarm.features.onboarding.permissions

sealed class PermissionsScreenBackendEvent {
    data object OnboardingFinished : PermissionsScreenBackendEvent()
}

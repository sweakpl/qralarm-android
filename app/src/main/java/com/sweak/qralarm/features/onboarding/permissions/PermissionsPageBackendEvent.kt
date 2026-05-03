package com.sweak.qralarm.features.onboarding.permissions

sealed class PermissionsPageBackendEvent {
    data object OnboardingFinished : PermissionsPageBackendEvent()
}

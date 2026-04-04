package com.sweak.qralarm.features.onboarding

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object WelcomeRoute : NavKey

@Serializable
data object SocialProofRoute : NavKey

@Serializable
data object IntroductionRoute : NavKey

@Serializable
data object PermissionsRoute : NavKey

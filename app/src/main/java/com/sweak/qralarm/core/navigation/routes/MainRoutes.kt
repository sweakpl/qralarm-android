package com.sweak.qralarm.core.navigation.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class IntroductionRoute(val isLaunchedFromMenu: Boolean) : NavKey

@Serializable
data object HomeRoute : NavKey

@Serializable
data class OptimizationRoute(val isLaunchedFromMenu: Boolean) : NavKey

@Serializable
data class AddEditAlarmRoute(val idOfAlarmToEdit: Long) : NavKey

@Serializable
data class AdvancedAlarmSettingsRoute(val idOfAlarmToEdit: Long) : NavKey

@Serializable
data class SpecialAlarmSettingsRoute(val idOfAlarmToEdit: Long) : NavKey

@Serializable
data class AlarmsChainSettingsRoute(val idOfAlarmToEdit: Long) : NavKey

@Serializable
data object EmergencySettingsRoute : NavKey

@Serializable
data object EmergencyTaskPreviewRoute : NavKey

@Serializable
data class CustomCodeScannerRoute(val shouldScanForDefaultCode: Boolean) : NavKey

@Serializable
data object QRAlarmProRoute : NavKey

@Serializable
data object RateRoute : NavKey

@Serializable
data object ThemeRoute : NavKey

@Serializable
data object MenuRoute : NavKey

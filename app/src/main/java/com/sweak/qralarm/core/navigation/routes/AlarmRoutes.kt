package com.sweak.qralarm.core.navigation.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class AlarmRoute(
    val idOfAlarm: Long,
    val isTransient: Boolean
) : NavKey

@Serializable
data class DisableAlarmScannerRoute(
    val idOfAlarm: Long,
    val isDisablingBeforeAlarmFired: Boolean
) : NavKey

@Serializable
data class EmergencyRoute(
    val idOfAlarmToCancel: Long
) : NavKey


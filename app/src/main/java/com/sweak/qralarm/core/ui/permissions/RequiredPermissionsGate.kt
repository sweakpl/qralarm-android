package com.sweak.qralarm.core.ui.permissions

import com.sweak.qralarm.core.domain.alarm.AlarmScheduler
import com.sweak.qralarm.core.ui.model.PermissionsDialogState
import javax.inject.Inject

class RequiredPermissionsGate @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    fun evaluate(
        dialogState: PermissionsDialogState,
        isCameraPermissionGranted: Boolean,
        isNotificationsPermissionGranted: Boolean,
        isCameraPermissionRequired: Boolean
    ): Decision {
        if (dialogState.isVisible) {
            if (dialogState.areAllPermissionsGranted) return Decision.Proceed

            return Decision.ShowDialog(
                dialogState.copy(
                    cameraPermissionState =
                        dialogState.cameraPermissionState?.let { isCameraPermissionGranted },
                    notificationsPermissionState =
                        dialogState.notificationsPermissionState?.let {
                            isNotificationsPermissionGranted
                        },
                    alarmsPermissionState =
                        dialogState.alarmsPermissionState?.let {
                            alarmScheduler.canScheduleExactAlarms()
                        },
                    fullScreenIntentPermissionState =
                        dialogState.fullScreenIntentPermissionState?.let {
                            alarmScheduler.canUseFullScreenIntent()
                        }
                )
            )
        }

        val isCameraMissing = isCameraPermissionRequired && !isCameraPermissionGranted
        val isNotificationsMissing = !isNotificationsPermissionGranted
        val isAlarmsMissing = !alarmScheduler.canScheduleExactAlarms()
        val isFullScreenIntentMissing = !alarmScheduler.canUseFullScreenIntent()

        if (!isCameraMissing && !isNotificationsMissing && !isAlarmsMissing &&
            !isFullScreenIntentMissing
        ) {
            return Decision.Proceed
        }

        return Decision.ShowDialog(
            PermissionsDialogState(
                isVisible = true,
                cameraPermissionState = if (isCameraMissing) false else null,
                notificationsPermissionState = if (isNotificationsMissing) false else null,
                alarmsPermissionState = if (isAlarmsMissing) false else null,
                fullScreenIntentPermissionState = if (isFullScreenIntentMissing) false else null
            )
        )
    }

    sealed interface Decision {
        data object Proceed : Decision
        data class ShowDialog(val dialogState: PermissionsDialogState) : Decision
    }
}

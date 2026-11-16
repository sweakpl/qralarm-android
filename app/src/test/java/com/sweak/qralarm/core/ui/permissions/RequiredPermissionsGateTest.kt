package com.sweak.qralarm.core.ui.permissions

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.sweak.qralarm.core.domain.alarm.AlarmScheduler
import com.sweak.qralarm.core.ui.model.PermissionsDialogState
import com.sweak.qralarm.core.ui.permissions.RequiredPermissionsGate.Decision
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class RequiredPermissionsGateTest {

    private val alarmScheduler = mockk<AlarmScheduler>(relaxUnitFun = true)
    private val requiredPermissionsGate = RequiredPermissionsGate(alarmScheduler = alarmScheduler)

    private fun givenSystemPermissions(
        canScheduleExactAlarms: Boolean,
        canUseFullScreenIntent: Boolean
    ) {
        every { alarmScheduler.canScheduleExactAlarms() } returns canScheduleExactAlarms
        every { alarmScheduler.canUseFullScreenIntent() } returns canUseFullScreenIntent
    }

    @Test
    fun `given hidden dialog and all permissions granted, when evaluating, then it proceeds`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = true, canUseFullScreenIntent = true)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(isVisible = false),
            isCameraPermissionGranted = true,
            isNotificationsPermissionGranted = true,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(Decision.Proceed)
    }

    @Test
    fun `given hidden dialog and camera denied but not required, when evaluating, then it proceeds`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = true, canUseFullScreenIntent = true)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(isVisible = false),
            isCameraPermissionGranted = false,
            isNotificationsPermissionGranted = true,
            isCameraPermissionRequired = false
        )

        // Then
        assertThat(decision).isEqualTo(Decision.Proceed)
    }

    @Test
    fun `given hidden dialog and required camera denied, when evaluating, then the dialog shows only the camera`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = true, canUseFullScreenIntent = true)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(isVisible = false),
            isCameraPermissionGranted = false,
            isNotificationsPermissionGranted = true,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(
            Decision.ShowDialog(
                PermissionsDialogState(
                    isVisible = true,
                    cameraPermissionState = false,
                    notificationsPermissionState = null,
                    alarmsPermissionState = null,
                    fullScreenIntentPermissionState = null
                )
            )
        )
    }

    @Test
    fun `given hidden dialog and notifications denied, when evaluating, then the dialog shows only notifications`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = true, canUseFullScreenIntent = true)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(isVisible = false),
            isCameraPermissionGranted = true,
            isNotificationsPermissionGranted = false,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(
            Decision.ShowDialog(
                PermissionsDialogState(
                    isVisible = true,
                    cameraPermissionState = null,
                    notificationsPermissionState = false,
                    alarmsPermissionState = null,
                    fullScreenIntentPermissionState = null
                )
            )
        )
    }

    @Test
    fun `given hidden dialog and exact alarms not allowed, when evaluating, then the dialog shows only alarms`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = false, canUseFullScreenIntent = true)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(isVisible = false),
            isCameraPermissionGranted = true,
            isNotificationsPermissionGranted = true,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(
            Decision.ShowDialog(
                PermissionsDialogState(
                    isVisible = true,
                    cameraPermissionState = null,
                    notificationsPermissionState = null,
                    alarmsPermissionState = false,
                    fullScreenIntentPermissionState = null
                )
            )
        )
    }

    @Test
    fun `given hidden dialog and full-screen intent not allowed, when evaluating, then the dialog shows only full-screen intent`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = true, canUseFullScreenIntent = false)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(isVisible = false),
            isCameraPermissionGranted = true,
            isNotificationsPermissionGranted = true,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(
            Decision.ShowDialog(
                PermissionsDialogState(
                    isVisible = true,
                    cameraPermissionState = null,
                    notificationsPermissionState = null,
                    alarmsPermissionState = null,
                    fullScreenIntentPermissionState = false
                )
            )
        )
    }

    @Test
    fun `given hidden dialog and every permission missing, when evaluating, then the dialog shows all four`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = false, canUseFullScreenIntent = false)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(isVisible = false),
            isCameraPermissionGranted = false,
            isNotificationsPermissionGranted = false,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(
            Decision.ShowDialog(
                PermissionsDialogState(
                    isVisible = true,
                    cameraPermissionState = false,
                    notificationsPermissionState = false,
                    alarmsPermissionState = false,
                    fullScreenIntentPermissionState = false
                )
            )
        )
    }

    @Test
    fun `given visible dialog with every listed permission granted, when evaluating, then it proceeds without re-checking unlisted ones`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = false, canUseFullScreenIntent = false)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(
                isVisible = true,
                cameraPermissionState = true,
                notificationsPermissionState = true,
                alarmsPermissionState = null,
                fullScreenIntentPermissionState = null
            ),
            isCameraPermissionGranted = true,
            isNotificationsPermissionGranted = true,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(Decision.Proceed)
    }

    @Test
    fun `given visible dialog with a listed permission still missing, when evaluating, then only listed permissions are refreshed`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = true, canUseFullScreenIntent = false)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(
                isVisible = true,
                cameraPermissionState = false,
                notificationsPermissionState = null,
                alarmsPermissionState = false,
                fullScreenIntentPermissionState = false
            ),
            isCameraPermissionGranted = true,
            isNotificationsPermissionGranted = false,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(
            Decision.ShowDialog(
                PermissionsDialogState(
                    isVisible = true,
                    cameraPermissionState = true,
                    notificationsPermissionState = null,
                    alarmsPermissionState = true,
                    fullScreenIntentPermissionState = false
                )
            )
        )
    }

    @Test
    fun `given visible dialog whose missing permissions were all just granted, when evaluating, then it refreshes the dialog instead of proceeding`() {
        // Given
        givenSystemPermissions(canScheduleExactAlarms = true, canUseFullScreenIntent = true)

        // When
        val decision = requiredPermissionsGate.evaluate(
            dialogState = PermissionsDialogState(
                isVisible = true,
                cameraPermissionState = false,
                notificationsPermissionState = null,
                alarmsPermissionState = null,
                fullScreenIntentPermissionState = null
            ),
            isCameraPermissionGranted = true,
            isNotificationsPermissionGranted = true,
            isCameraPermissionRequired = true
        )

        // Then
        assertThat(decision).isEqualTo(
            Decision.ShowDialog(
                PermissionsDialogState(
                    isVisible = true,
                    cameraPermissionState = true,
                    notificationsPermissionState = null,
                    alarmsPermissionState = null,
                    fullScreenIntentPermissionState = null
                )
            )
        )
    }
}

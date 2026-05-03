package com.sweak.qralarm.features.onboarding.permissions

import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.features.onboarding.permissions.util.PermissionsPagePermissionKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val qrAlarmManager: QRAlarmManager,
    private val powerManager: PowerManager,
    @param:Named("PackageName") private val packageName: String,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PermissionsPageState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<PermissionsPageBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    init {
        val alarmsVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2
        val notificationsVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val fullScreenVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

        _state.update { current ->
            current.copy(
                alarmsPermissionVisible = alarmsVisible,
                notificationsPermissionVisible = notificationsVisible,
                fullScreenIntentPermissionVisible = fullScreenVisible,
                notificationsPermissionGranted = !notificationsVisible
            )
        }

        refreshSystemPermissionsOnly()
    }

    fun refresh() {
        refreshSystemPermissionsOnly()
    }

    private fun refreshSystemPermissionsOnly() {
        _state.update { current ->
            current.copy(
                alarmsPermissionGranted = if (current.alarmsPermissionVisible) {
                    qrAlarmManager.canScheduleExactAlarms()
                } else {
                    true
                },
                fullScreenIntentPermissionGranted = if (current.fullScreenIntentPermissionVisible) {
                    qrAlarmManager.canUseFullScreenIntent()
                } else {
                    true
                },
                backgroundWorkPermissionGranted =
                    powerManager.isIgnoringBatteryOptimizations(packageName)
            )
        }

        recomputePermissionsRequiringInteraction()
    }

    fun onEvent(event: PermissionsPageUserEvent) {
        when (event) {
            is PermissionsPageUserEvent.PermissionsUpdated -> {
                _state.update { current ->
                    current.copy(
                        cameraPermissionGranted = event.cameraPermissionGranted,
                        notificationsPermissionGranted = event.notificationsPermissionGranted
                    )
                }

                recomputePermissionsRequiringInteraction()
            }

            is PermissionsPageUserEvent.CameraPermissionClicked ->
                addInteraction(PermissionsPagePermissionKey.CAMERA)

            is PermissionsPageUserEvent.AlarmsPermissionClicked ->
                addInteraction(PermissionsPagePermissionKey.ALARMS)

            is PermissionsPageUserEvent.NotificationsPermissionClicked ->
                addInteraction(PermissionsPagePermissionKey.NOTIFICATIONS)

            is PermissionsPageUserEvent.FullScreenIntentPermissionClicked ->
                addInteraction(PermissionsPagePermissionKey.FULL_SCREEN_INTENT)

            is PermissionsPageUserEvent.BackgroundWorkPermissionClicked ->
                addInteraction(PermissionsPagePermissionKey.BACKGROUND_WORK)

            is PermissionsPageUserEvent.CameraPermissionDeniedDialogVisible ->
                _state.update { current ->
                    current.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }

            is PermissionsPageUserEvent.NotificationsPermissionDeniedDialogVisible ->
                _state.update { current ->
                    current.copy(isNotificationsPermissionDeniedDialogVisible = event.isVisible)
                }

            is PermissionsPageUserEvent.GoToApplicationSettingsClicked ->
                _state.update { current ->
                    current.copy(
                        isCameraPermissionDeniedDialogVisible = false,
                        isNotificationsPermissionDeniedDialogVisible = false
                    )
                }

            is PermissionsPageUserEvent.LetsGoClicked -> {
                viewModelScope.launch {
                    userDataRepository.setIntroductionFinished(finished = true)
                    backendEventsChannel.send(PermissionsPageBackendEvent.OnboardingFinished)
                }
            }
        }
    }

    private fun addInteraction(key: PermissionsPagePermissionKey) {
        _state.update { current ->
            val newInteracted = current.interactedPermissions + key

            current.copy(
                interactedPermissions = newInteracted,
                areAllRequiredPermissionsHandled = current.permissionsRequiringInteraction.all {
                    it in newInteracted
                }
            )
        }
    }

    private fun recomputePermissionsRequiringInteraction() {
        _state.update { current ->
            val notificationsEffective = if (current.notificationsPermissionVisible) {
                current.notificationsPermissionGranted
            } else {
                true
            }

            val requiring = buildSet {
                if (!current.cameraPermissionGranted) {
                    add(PermissionsPagePermissionKey.CAMERA)
                }

                if (current.alarmsPermissionVisible && !current.alarmsPermissionGranted) {
                    add(PermissionsPagePermissionKey.ALARMS)
                }

                if (current.notificationsPermissionVisible && !notificationsEffective) {
                    add(PermissionsPagePermissionKey.NOTIFICATIONS)
                }

                if (current.fullScreenIntentPermissionVisible &&
                    !current.fullScreenIntentPermissionGranted
                ) {
                    add(PermissionsPagePermissionKey.FULL_SCREEN_INTENT)
                }

                if (!current.backgroundWorkPermissionGranted) {
                    add(PermissionsPagePermissionKey.BACKGROUND_WORK)
                }
            }

            val newInteracted = current.interactedPermissions.filter { it in requiring }.toSet()

            current.copy(
                permissionsRequiringInteraction = requiring,
                interactedPermissions = newInteracted,
                areAllRequiredPermissionsHandled = requiring.all { it in newInteracted }
            )
        }
    }
}

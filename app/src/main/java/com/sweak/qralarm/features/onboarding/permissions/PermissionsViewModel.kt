package com.sweak.qralarm.features.onboarding.permissions

import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.features.onboarding.permissions.util.OnboardingPermissionKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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

    private val _state = MutableStateFlow(PermissionsScreenState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<PermissionsScreenBackendEvent>()
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
        viewModelScope.launch {
            delay(timeMillis = 1000L)
            refreshSystemPermissionsOnly()
        }
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

    fun onEvent(event: PermissionsScreenUserEvent) {
        when (event) {
            is PermissionsScreenUserEvent.PermissionsUpdated -> {
                _state.update { current ->
                    current.copy(
                        cameraPermissionGranted = event.cameraPermissionGranted,
                        notificationsPermissionGranted = event.notificationsPermissionGranted
                    )
                }

                recomputePermissionsRequiringInteraction()
            }

            is PermissionsScreenUserEvent.CameraPermissionClicked -> {
                addInteraction(OnboardingPermissionKey.CAMERA)
            }

            is PermissionsScreenUserEvent.AlarmsPermissionClicked -> {
                addInteraction(OnboardingPermissionKey.ALARMS)
            }

            is PermissionsScreenUserEvent.NotificationsPermissionClicked -> {
                addInteraction(OnboardingPermissionKey.NOTIFICATIONS)
            }

            is PermissionsScreenUserEvent.FullScreenIntentPermissionClicked -> {
                addInteraction(OnboardingPermissionKey.FULL_SCREEN_INTENT)
            }

            is PermissionsScreenUserEvent.BackgroundWorkPermissionClicked -> {
                addInteraction(OnboardingPermissionKey.BACKGROUND_WORK)
            }

            is PermissionsScreenUserEvent.CameraPermissionDeniedDialogVisible -> {
                _state.update { current ->
                    current.copy(isCameraPermissionDeniedDialogVisible = event.isVisible)
                }
            }

            is PermissionsScreenUserEvent.NotificationsPermissionDeniedDialogVisible -> {
                _state.update { current ->
                    current.copy(isNotificationsPermissionDeniedDialogVisible = event.isVisible)
                }
            }

            is PermissionsScreenUserEvent.LetsGoClicked -> {
                viewModelScope.launch {
                    userDataRepository.setIntroductionFinished(finished = true)
                    backendEventsChannel.send(
                        PermissionsScreenBackendEvent.OnboardingFinished
                    )
                }
            }

            is PermissionsScreenUserEvent.GoToApplicationSettingsClicked -> Unit
        }
    }

    private fun addInteraction(key: String) {
        _state.update { current ->
            val newInteracted = current.interactedPermissions + key

            current.copy(
                interactedPermissions = newInteracted,
                isLetsGoButtonEnabled = current.permissionsRequiringInteraction.all {
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
                    add(OnboardingPermissionKey.CAMERA)
                }

                if (current.alarmsPermissionVisible && !current.alarmsPermissionGranted) {
                    add(OnboardingPermissionKey.ALARMS)
                }

                if (current.notificationsPermissionVisible && !notificationsEffective) {
                    add(OnboardingPermissionKey.NOTIFICATIONS)
                }

                if (current.fullScreenIntentPermissionVisible &&
                    !current.fullScreenIntentPermissionGranted
                ) {
                    add(OnboardingPermissionKey.FULL_SCREEN_INTENT)
                }

                if (!current.backgroundWorkPermissionGranted) {
                    add(OnboardingPermissionKey.BACKGROUND_WORK)
                }
            }

            val newInteracted = current.interactedPermissions.filter { it in requiring }.toSet()

            current.copy(
                permissionsRequiringInteraction = requiring,
                interactedPermissions = newInteracted,
                isLetsGoButtonEnabled = requiring.all { it in newInteracted }
            )
        }
    }
}

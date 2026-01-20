package com.sweak.qralarm.features.alarm

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.service.AlarmService.Companion.ACTION_TEMPORARY_ALARM_MUTE
import com.sweak.qralarm.alarm.service.AlarmService.Companion.EXTRA_TEMPORARY_MUTE_DURATION_SECONDS
import com.sweak.qralarm.core.designsystem.component.QRAlarmDialog
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.components.MissingPermissionsBottomSheet
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.core.ui.compose_util.UiText
import com.sweak.qralarm.core.ui.getTimeString
import com.sweak.qralarm.features.alarm.components.TimeTickReceiver

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AlarmScreen(
    onStopAlarm: () -> Unit,
    onRequestCodeScan: () -> Unit,
    onSnoozeAlarm: () -> Unit,
    onEmergencyClicked: () -> Unit
) {
    val alarmViewModel = hiltViewModel<AlarmViewModel>()
    val alarmScreenState by alarmViewModel.state.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )
    val context = LocalContext.current

    ObserveAsEvents(
        flow = alarmViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is AlarmScreenBackendEvent.StopAlarm -> onStopAlarm()
                is AlarmScreenBackendEvent.RequestCodeScanToStopAlarm -> onRequestCodeScan()
                is AlarmScreenBackendEvent.SnoozeAlarm -> onSnoozeAlarm()
                is AlarmScreenBackendEvent.TryTemporarilyMuteAlarm -> {
                    context.sendBroadcast(
                        Intent(ACTION_TEMPORARY_ALARM_MUTE).apply {
                            setPackage(context.packageName)
                            putExtra(
                                EXTRA_TEMPORARY_MUTE_DURATION_SECONDS,
                                event.muteDurationInSeconds
                            )
                        }
                    )
                }
            }
        }
    )

    OnResume {
        if (alarmScreenState.permissionsDialogState.isVisible) {
            alarmViewModel.onEvent(
                AlarmScreenUserEvent.TryStopAlarm(
                    cameraPermissionStatus = cameraPermissionState.status.isGranted
                )
            )
        }
    }

    TimeTickReceiver { alarmViewModel.onEvent(AlarmScreenUserEvent.UpdateCurrentTime) }

    AlarmScreenContent(
        state = alarmScreenState,
        onEvent = { event ->
            when (event) {
                is AlarmScreenUserEvent.StopAlarmClicked -> {
                    alarmViewModel.onEvent(
                        AlarmScreenUserEvent.TryStopAlarm(
                            cameraPermissionStatus = cameraPermissionState.status.isGranted
                        )
                    )
                }
                is AlarmScreenUserEvent.RequestCameraPermission -> {
                    if (cameraPermissionState.status.shouldShowRationale) {
                        alarmViewModel.onEvent(
                            AlarmScreenUserEvent.CameraPermissionDeniedDialogVisible(
                                isVisible = true
                            )
                        )
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
                is AlarmScreenUserEvent.GoToApplicationSettingsClicked -> {
                    alarmViewModel.onEvent(
                        AlarmScreenUserEvent.CameraPermissionDeniedDialogVisible(
                            isVisible = false
                        )
                    )
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    )
                }
                is AlarmScreenUserEvent.OnEmergencyClicked -> onEmergencyClicked()
                else -> alarmViewModel.onEvent(event)
            }
        }
    )
}

@Composable
private fun AlarmScreenContent(
    state: AlarmScreenState,
    onEvent: (AlarmScreenUserEvent) -> Unit
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (MaterialTheme.isQRAlarmTheme)
                        Modifier.background(
                            brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac))
                        )
                    else Modifier
                )
                .padding(paddingValues = paddingValues)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.xLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(visible = state.alarmLabel != null) {
                        Text(
                            text = state.alarmLabel?.asString() ?: "",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displayLarge
                        )
                    }

                    Text(
                        text = getTimeString(
                            timeInMillis = state.timeToShow,
                            is24HourFormat = DateFormat.is24HourFormat(LocalContext.current)
                        ),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { onEvent(AlarmScreenUserEvent.StopAlarmClicked) },
                        enabled = state.isInteractionEnabled,
                    ) {
                        Text(
                            text = stringResource(R.string.stop),
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.padding(all = MaterialTheme.space.small)
                        )
                    }

                    AnimatedVisibility(visible = state.isSnoozeAvailable) {
                        val snoozeButtonColor =
                            if (MaterialTheme.isQRAlarmTheme) Color.White
                            else LocalContentColor.current

                        TextButton(
                            onClick = { onEvent(AlarmScreenUserEvent.SnoozeAlarmClicked) },
                            enabled = state.isInteractionEnabled,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = snoozeButtonColor
                            ),
                            modifier = Modifier.padding(top = MaterialTheme.space.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.snooze_capitals),
                                style = MaterialTheme.typography.displaySmall,
                                modifier = Modifier.padding(all = MaterialTheme.space.small)
                            )
                        }
                    }
                }
            }

            if (state.isEmergencyAvailable) {
                IconButton(
                    onClick = { onEvent(AlarmScreenUserEvent.OnEmergencyClicked) },
                    enabled = state.isInteractionEnabled,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(all = MaterialTheme.space.small)
                ) {
                    Icon(
                        imageVector = QRAlarmIcons.Emergency,
                        contentDescription =
                            stringResource(R.string.content_description_emergency_icon)
                    )
                }
            }
        }
    }

    if (state.permissionsDialogState.isVisible) {
        MissingPermissionsBottomSheet(
            cameraPermissionState = state.permissionsDialogState.cameraPermissionState,
            onCameraPermissionClick = {
                onEvent(AlarmScreenUserEvent.RequestCameraPermission)
            },
            onAllPermissionsGranted = { onEvent(AlarmScreenUserEvent.StopAlarmClicked) },
            onDismissRequest = { onEvent(AlarmScreenUserEvent.HideMissingPermissionsDialog) }
        )
    }

    if (state.isCameraPermissionDeniedDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.camera_permission_required),
            message = stringResource(R.string.camera_permission_required_description),
            onDismissRequest = {
                onEvent(AlarmScreenUserEvent.CameraPermissionDeniedDialogVisible(isVisible = false))
            },
            onPositiveClick = {
                onEvent(AlarmScreenUserEvent.GoToApplicationSettingsClicked)
            },
            positiveButtonText = stringResource(R.string.settings),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun AlarmScreenContentPreview() {
    QRAlarmTheme {
        AlarmScreenContent(
            state = AlarmScreenState(
                alarmLabel = UiText.DynamicString("Alarm label"),
                isSnoozeAvailable = true,
                timeToShow = 1729861439787
            ),
            onEvent = {}
        )
    }
}
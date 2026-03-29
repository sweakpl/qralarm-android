package com.sweak.qralarm.features.onboarding.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmDialog
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.features.onboarding.components.PermissionCard
import com.sweak.qralarm.features.onboarding.permissions.util.OnboardingPermissionKey

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("BatteryLife")
@Composable
fun PermissionsScreen(onOnboardingFinished: () -> Unit) {
    val viewModel = hiltViewModel<PermissionsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val resources = LocalResources.current

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val notificationsPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
        } else {
            object : PermissionState {
                override val permission: String
                    get() = "android.permission.POST_NOTIFICATIONS"
                override val status: PermissionStatus
                    get() = PermissionStatus.Granted

                override fun launchPermissionRequest() { /* no-op */ }
            }
        }

    LaunchedEffect(
        cameraPermissionState.status,
        notificationsPermissionState.status
    ) {
        viewModel.onEvent(
            PermissionsScreenUserEvent.PermissionsUpdated(
                cameraPermissionGranted = cameraPermissionState.status.isGranted,
                notificationsPermissionGranted = notificationsPermissionState.status.isGranted
            )
        )
    }

    OnResume { viewModel.refresh() }

    ObserveAsEvents(flow = viewModel.backendEvents) { event ->
        when (event) {
            PermissionsScreenBackendEvent.OnboardingFinished -> onOnboardingFinished()
        }
    }

    PermissionsScreenContent(
        state = state,
        onEvent = { event ->
            when (event) {
                is PermissionsScreenUserEvent.CameraPermissionClicked -> {
                    viewModel.onEvent(event)
                    if (cameraPermissionState.status.shouldShowRationale) {
                        viewModel.onEvent(
                            PermissionsScreenUserEvent.CameraPermissionDeniedDialogVisible(
                                isVisible = true
                            )
                        )
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }

                is PermissionsScreenUserEvent.AlarmsPermissionClicked -> {
                    viewModel.onEvent(event)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                data = "package:${context.packageName}".toUri()
                            }
                        )
                    }
                }

                is PermissionsScreenUserEvent.NotificationsPermissionClicked -> {
                    viewModel.onEvent(event)
                    if (notificationsPermissionState.status.shouldShowRationale) {
                        viewModel.onEvent(
                            PermissionsScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                                isVisible = true
                            )
                        )
                    } else {
                        notificationsPermissionState.launchPermissionRequest()
                    }
                }

                is PermissionsScreenUserEvent.FullScreenIntentPermissionClicked -> {
                    viewModel.onEvent(event)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
                                data = "package:${context.packageName}".toUri()
                            }
                        )
                    }
                }

                is PermissionsScreenUserEvent.BackgroundWorkPermissionClicked -> {
                    viewModel.onEvent(event)
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                        )
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            resources.getString(
                                R.string.setting_unavailable_refer_to_the_next_step
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                is PermissionsScreenUserEvent.GoToApplicationSettingsClicked -> {
                    viewModel.onEvent(
                        PermissionsScreenUserEvent.CameraPermissionDeniedDialogVisible(
                            isVisible = false
                        )
                    )
                    viewModel.onEvent(
                        PermissionsScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                            isVisible = false
                        )
                    )
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    )
                }

                else -> viewModel.onEvent(event)
            }
        }
    )
}

@Composable
private fun PermissionsScreenContent(
    state: PermissionsScreenState,
    onEvent: (PermissionsScreenUserEvent) -> Unit
) {
    val bottomInsetForButton = MaterialTheme.space.run { mediumLarge + xLarge + mediumLarge }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac)))
                .padding(paddingValues = paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.almost_ready_to_go),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            top = MaterialTheme.space.mediumLarge,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.medium
                        )
                )

                Text(
                    text = stringResource(R.string.permissions_screen_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.mediumLarge
                        )
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = LocalContentColor.current,
                    modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                )

                PermissionCard(
                    icon = QRAlarmIcons.Camera,
                    iconContentDescription =
                        stringResource(R.string.content_description_camera_icon),
                    title = stringResource(R.string.camera),
                    subtitle = stringResource(R.string.camera_usage),
                    isGranted = state.cameraPermissionGranted,
                    onClick = { onEvent(PermissionsScreenUserEvent.CameraPermissionClicked) },
                    isClickable = !state.cameraPermissionGranted,
                    modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                )

                if (state.alarmsPermissionVisible) {
                    PermissionCard(
                        icon = QRAlarmIcons.Alarm,
                        iconContentDescription =
                            stringResource(R.string.content_description_alarm_icon),
                        title = stringResource(R.string.alarms),
                        subtitle = stringResource(R.string.alarms_usage),
                        isGranted = state.alarmsPermissionGranted,
                        onClick = { onEvent(PermissionsScreenUserEvent.AlarmsPermissionClicked) },
                        isClickable = !state.alarmsPermissionGranted,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    )
                }

                if (state.notificationsPermissionVisible) {
                    PermissionCard(
                        icon = QRAlarmIcons.Notification,
                        iconContentDescription = stringResource(
                            R.string.content_description_notification_icon
                        ),
                        title = stringResource(R.string.notifications),
                        subtitle = stringResource(R.string.notifications_usage),
                        isGranted = state.notificationsPermissionGranted,
                        onClick = {
                            onEvent(PermissionsScreenUserEvent.NotificationsPermissionClicked)
                        },
                        isClickable = !state.notificationsPermissionGranted,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    )
                }

                if (state.fullScreenIntentPermissionVisible) {
                    PermissionCard(
                        icon = QRAlarmIcons.FullScreen,
                        iconContentDescription = stringResource(
                            R.string.content_description_full_screen_icon
                        ),
                        title = stringResource(R.string.full_screen_display),
                        subtitle = stringResource(R.string.full_screen_display_usage),
                        isGranted = state.fullScreenIntentPermissionGranted,
                        onClick = {
                            onEvent(PermissionsScreenUserEvent.FullScreenIntentPermissionClicked)
                        },
                        isClickable = !state.fullScreenIntentPermissionGranted,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    )
                }

                PermissionCard(
                    icon = QRAlarmIcons.AutomaticSettings,
                    iconContentDescription = stringResource(
                        R.string.content_description_automatic_settings_icon
                    ),
                    title = stringResource(R.string.background_work),
                    subtitle = stringResource(R.string.enable_background_work_description),
                    isGranted = state.backgroundWorkPermissionGranted,
                    onClick = {
                        onEvent(PermissionsScreenUserEvent.BackgroundWorkPermissionClicked)
                    },
                    isClickable = !state.backgroundWorkPermissionGranted,
                    modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                )

                Spacer(modifier = Modifier.height(bottomInsetForButton))
            }

            Button(
                onClick = { onEvent(PermissionsScreenUserEvent.LetsGoClicked) },
                enabled = state.isLetsGoButtonEnabled,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.space.mediumLarge,
                        end = MaterialTheme.space.mediumLarge,
                        bottom = MaterialTheme.space.mediumLarge
                    )
            ) {
                Text(
                    text = stringResource(R.string.lets_go),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    if (state.isCameraPermissionDeniedDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.camera_permission_required),
            message = stringResource(R.string.camera_permission_required_description),
            onDismissRequest = {
                onEvent(
                    PermissionsScreenUserEvent.CameraPermissionDeniedDialogVisible(
                        isVisible = false
                    )
                )
            },
            onPositiveClick = {
                onEvent(PermissionsScreenUserEvent.GoToApplicationSettingsClicked)
            },
            positiveButtonText = stringResource(R.string.settings),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }

    if (state.isNotificationsPermissionDeniedDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.notifications_permission_required),
            message = stringResource(R.string.notifications_permission_required_description),
            onDismissRequest = {
                onEvent(
                    PermissionsScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                        isVisible = false
                    )
                )
            },
            onPositiveClick = {
                onEvent(PermissionsScreenUserEvent.GoToApplicationSettingsClicked)
            },
            positiveButtonText = stringResource(R.string.settings),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }
}

@Preview
@Composable
private fun PermissionsScreenContentPreview() {
    QRAlarmTheme {
        PermissionsScreenContent(
            state = PermissionsScreenState(
                cameraPermissionGranted = true,
                alarmsPermissionVisible = true,
                alarmsPermissionGranted = false,
                notificationsPermissionVisible = true,
                notificationsPermissionGranted = false,
                fullScreenIntentPermissionVisible = true,
                fullScreenIntentPermissionGranted = false,
                backgroundWorkPermissionGranted = false,
                permissionsRequiringInteraction = setOf(
                    OnboardingPermissionKey.ALARMS,
                    OnboardingPermissionKey.NOTIFICATIONS,
                    OnboardingPermissionKey.FULL_SCREEN_INTENT,
                    OnboardingPermissionKey.BACKGROUND_WORK
                ),
                interactedPermissions = emptySet(),
                isLetsGoButtonEnabled = false
            ),
            onEvent = {}
        )
    }
}

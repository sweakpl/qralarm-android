package com.sweak.qralarm.features.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.components.MissingPermissionsBottomSheet
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.features.home.components.AlarmCard
import com.sweak.qralarm.features.home.components.model.AlarmWrapper

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onAddNewAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit
) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val homeScreenState by homeViewModel.state.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )
    val notificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        object : PermissionState {
            override val permission: String get() = "android.permission.POST_NOTIFICATIONS"
            override val status: PermissionStatus get() = PermissionStatus.Granted
            override fun launchPermissionRequest() { /* no-op */ }
        }
    }

    val context = LocalContext.current

    ObserveAsEvents(
        flow = homeViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is HomeScreenBackendEvent.AlarmSet -> {
                    val days = event.daysHoursAndMinutesUntilAlarm.first
                    val hours = event.daysHoursAndMinutesUntilAlarm.second
                    val minutes = event.daysHoursAndMinutesUntilAlarm.third
                    val resources = context.resources

                    Toast.makeText(
                        context,
                        buildString {
                            append(context.getString(R.string.alarm_in))
                            append(' ')
                            if (days != 0) {
                                append(resources.getQuantityString(R.plurals.days, days, days))
                                append(' ')
                            }
                            if (hours != 0 || days != 0) {
                                append(resources.getQuantityString(R.plurals.hours, hours, hours))
                                append(' ')
                            }
                            append(
                                resources.getQuantityString(
                                    R.plurals.minutes,
                                    if (minutes == 0) 1 else minutes,
                                    if (minutes == 0) 1 else minutes
                                )
                            )
                        },
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    OnResume {
        homeViewModel.onEvent(
            HomeScreenUserEvent.TryChangeAlarmEnabled(
                cameraPermissionStatus = cameraPermissionState.status.isGranted,
                notificationsPermissionStatus =
                notificationsPermissionState.status.isGranted
            )
        )
    }

    HomeScreenContent(
        state = homeScreenState,
        onEvent = { event ->
            when (event) {
                is HomeScreenUserEvent.AddNewAlarm -> onAddNewAlarm()
                is HomeScreenUserEvent.EditAlarm -> onEditAlarm(event.alarmId)
                is HomeScreenUserEvent.AlarmEnabledChangeClicked -> {
                    homeViewModel.onEvent(
                        event = HomeScreenUserEvent.TryChangeAlarmEnabled(
                            alarmId = event.alarmId,
                            enabled = event.enabled,
                            cameraPermissionStatus = cameraPermissionState.status.isGranted,
                            notificationsPermissionStatus =
                            notificationsPermissionState.status.isGranted
                        )
                    )
                }
                is HomeScreenUserEvent.RequestCameraPermission -> {
                    if (cameraPermissionState.status.shouldShowRationale) {
                        homeViewModel.onEvent(
                            HomeScreenUserEvent.CameraPermissionDeniedDialogVisible(
                                isVisible = true
                            )
                        )
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
                is HomeScreenUserEvent.RequestNotificationsPermission -> {
                    if (notificationsPermissionState.status.shouldShowRationale) {
                        homeViewModel.onEvent(
                            HomeScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                                isVisible = true
                            )
                        )
                    } else {
                        notificationsPermissionState.launchPermissionRequest()
                    }
                }
                is HomeScreenUserEvent.RequestAlarmsPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                }
                is HomeScreenUserEvent.RequestFullScreenIntentPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                }
                is HomeScreenUserEvent.GoToApplicationSettingsClicked -> {
                    homeViewModel.onEvent(
                        HomeScreenUserEvent.CameraPermissionDeniedDialogVisible(
                            isVisible = false
                        )
                    )
                    homeViewModel.onEvent(
                        HomeScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                            isVisible = false
                        )
                    )
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                }
                else -> homeViewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    state: HomeScreenState,
    onEvent: (HomeScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { /* TODO */ }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.Menu,
                            contentDescription =
                            stringResource(R.string.content_description_menu_icon)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(HomeScreenUserEvent.AddNewAlarm) },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    imageVector = QRAlarmIcons.Add,
                    contentDescription = stringResource(R.string.content_description_add_icon),
                    modifier = Modifier.size(size = MaterialTheme.space.large)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            LazyColumn(modifier = Modifier.padding(paddingValues = paddingValues)) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                top = MaterialTheme.space.medium,
                                end = MaterialTheme.space.small,
                                bottom = MaterialTheme.space.mediumLarge
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.alarms),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        IconButton(
                            onClick = { onEvent(HomeScreenUserEvent.AddNewAlarm) }
                        ) {
                            Icon(
                                imageVector = QRAlarmIcons.Add,
                                contentDescription = stringResource(R.string.content_description_add_icon),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(size = MaterialTheme.space.large)
                            )
                        }
                    }
                }

                items(
                    items = state.alarmWrappers,
                    key = { it.alarmId }
                ) {
                    AlarmCard(
                        alarmWrapper = it,
                        onClick = { alarmId ->
                            onEvent(HomeScreenUserEvent.EditAlarm(alarmId = alarmId))
                        },
                        onAlarmEnabledChanged = { alarmId, enabled ->
                            onEvent(
                                HomeScreenUserEvent.AlarmEnabledChangeClicked(
                                    alarmId = alarmId,
                                    enabled = enabled
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.medium
                            )
                            .animateItem()
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier.height(
                            MaterialTheme.space.run { xxLarge + small }
                        )
                    )
                }
            }
        }
    }

    if (state.permissionsDialogState.isVisible) {
        MissingPermissionsBottomSheet(
            cameraPermissionState = state.permissionsDialogState.cameraPermissionState,
            onCameraPermissionClick = {
                onEvent(HomeScreenUserEvent.RequestCameraPermission)
            },
            alarmsPermissionState = state.permissionsDialogState.alarmsPermissionState,
            onAlarmsPermissionClick = {
                onEvent(HomeScreenUserEvent.RequestAlarmsPermission)
            },
            notificationsPermissionState = state.permissionsDialogState.notificationsPermissionState,
            onNotificationsPermissionClick = {
                onEvent(HomeScreenUserEvent.RequestNotificationsPermission)
            },
            fullScreenIntentPermissionState =
            state.permissionsDialogState.fullScreenIntentPermissionState,
            onFullScreenIntentPermissionClick = {
                onEvent(HomeScreenUserEvent.RequestFullScreenIntentPermission)
            },
            onAllPermissionsGranted = { onEvent(HomeScreenUserEvent.AlarmEnabledChangeClicked()) },
            onDismissRequest = { onEvent(HomeScreenUserEvent.HideMissingPermissionsDialog) }
        )
    }

    if (state.isCameraPermissionDeniedDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.camera_permission_required),
            message = stringResource(R.string.camera_permission_required_description),
            onDismissRequest = {
                onEvent(HomeScreenUserEvent.CameraPermissionDeniedDialogVisible(isVisible = false))
            },
            onPositiveClick = {
                onEvent(HomeScreenUserEvent.GoToApplicationSettingsClicked)
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
                    HomeScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                        isVisible = false
                    )
                )
            },
            onPositiveClick = {
                onEvent(HomeScreenUserEvent.GoToApplicationSettingsClicked)
            },
            positiveButtonText = stringResource(R.string.settings),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }
}

@Preview
@Composable
private fun HomeScreenContentPreview() {
    QRAlarmTheme {
        HomeScreenContent(
            state = HomeScreenState(
                alarmWrappers = listOf(
                    AlarmWrapper(
                        alarmId = 0,
                        alarmHourOfDay = 8,
                        alarmMinute = 0,
                        alarmRepeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(),
                        isAlarmEnabled = true,
                        isCodeEnabled = false
                    )
                )
            ),
            onEvent = {}
        )
    }
}
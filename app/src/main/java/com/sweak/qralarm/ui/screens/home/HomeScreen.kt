package com.sweak.qralarm.ui.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.components.AlarmPermissionDialog
import com.sweak.qralarm.ui.screens.components.CameraPermissionSetAlarmDialog
import com.sweak.qralarm.ui.screens.components.CameraPermissionSetAlarmRevokedDialog
import com.sweak.qralarm.ui.screens.components.CodePossessionConfirmationDialog
import com.sweak.qralarm.ui.screens.components.DisableRepeatingAlarmsDialog
import com.sweak.qralarm.ui.screens.components.EnableRepeatingAlarmsDialog
import com.sweak.qralarm.ui.screens.components.FullScreenIntentPermissionDialog
import com.sweak.qralarm.ui.screens.components.NotificationsPermissionDialog
import com.sweak.qralarm.ui.screens.components.NotificationsPermissionRevokedDialog
import com.sweak.qralarm.ui.screens.components.QRAlarmTimePicker
import com.sweak.qralarm.ui.screens.navigateThrottled
import com.sweak.qralarm.ui.theme.amikoFamily
import com.sweak.qralarm.ui.theme.space
import com.sweak.qralarm.util.ACTION_TEMPORARY_ALARM_SOUND_MUTE
import com.sweak.qralarm.util.Screen
import com.sweak.qralarm.util.TimeFormat

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    finishableActionSideEffect: () -> Unit,
    context: Context = LocalContext.current
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState = remember { homeViewModel.homeUiState }
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val notificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        object : PermissionState {
            override val permission: String get() = "android.permission.POST_NOTIFICATIONS"
            override val status: PermissionStatus get() = PermissionStatus.Granted
            override fun launchPermissionRequest() { /* no-op */ }
        }
    }
    val composableScope = rememberCoroutineScope()

    val constraints = ConstraintSet {
        val menuButton = createRefFor("menuButton")
        val alarmAtText = createRefFor("alarmAtText")
        val timePicker = createRefFor("timePicker")
        val startStopAlarmButton = createRefFor("startStopAlarmButton")
        val repeatAlarmButton = createRefFor("repeatAlarmButton")
        val snoozeButton = createRefFor("snoozeButton")

        constrain(menuButton) {
            top.linkTo(parent.top)
            end.linkTo(parent.end)
        }

        constrain(alarmAtText) {
            bottom.linkTo(timePicker.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(timePicker) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(startStopAlarmButton) {
            top.linkTo(timePicker.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(repeatAlarmButton) {
            top.linkTo(timePicker.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(startStopAlarmButton.end)
            end.linkTo(parent.end)
        }

        constrain(snoozeButton) {
            top.linkTo(startStopAlarmButton.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.value.alarmSet) {
        if (!uiState.value.alarmSet) {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        action = {
                            TextButton(
                                onClick = { snackbarData.performAction() }
                            ) {
                                Text(
                                    text = snackbarData.visuals.actionLabel
                                        ?: stringResource(R.string.cancel_capitals),
                                )
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(all = MaterialTheme.space.medium)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.alarm_set),
                                style = MaterialTheme.typography.displayMedium
                            )

                            Text(
                                text = snackbarData.visuals.message,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = MaterialTheme.space.small)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        ConstraintLayout(
            constraints,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            AnimatedVisibility(
                visible = !uiState.value.alarmSet && !uiState.value.alarmServiceRunning,
                modifier = Modifier
                    .padding(top = paddingValues.calculateTopPadding())
                    .layoutId("menuButton")
            ) {
                MenuButton(
                    modifier = Modifier.padding(MaterialTheme.space.medium),
                    navController = navController
                ) {
                    homeViewModel.shouldNotUpdateAlarmStateAfterDataStoreManagerUpdate = true
                }
            }

            Text(
                text = stringResource(
                    if (uiState.value.alarmSet || uiState.value.alarmServiceRunning) R.string.alarm_at
                    else R.string.drag_to_set_alarm
                ),
                modifier = Modifier
                    .layoutId("alarmAtText")
                    .padding(horizontal = MaterialTheme.space.medium),
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                fontFamily = amikoFamily,
                fontWeight = FontWeight.SemiBold
            )

            TimePicker(
                modifier = Modifier.layoutId("timePicker"),
                uiState = uiState
            )

            StartStopAlarmButton(
                modifier = Modifier.layoutId("startStopAlarmButton"),
                uiState = uiState
            ) {
                homeViewModel.handleStartOrStopButtonClick(
                    navController,
                    cameraPermissionState,
                    notificationsPermissionState,
                    composableScope,
                    lifecycleOwner,
                    snackbarInitializer = { hoursAndMinutesUntilAlarmPair ->
                        snackbarHostState.showSnackbar(
                            message = if (hoursAndMinutesUntilAlarmPair.first > 0)
                                context.getString(
                                    R.string.time_left_hours_minutes,
                                    hoursAndMinutesUntilAlarmPair.first,
                                    hoursAndMinutesUntilAlarmPair.second
                                )
                            else
                                context.getString(
                                    R.string.time_left_minutes,
                                    hoursAndMinutesUntilAlarmPair.second
                                ),
                            actionLabel = context.getString(R.string.cancel_capitals),
                            duration = SnackbarDuration.Long
                        )
                    },
                    handleAlarmMute = {
                        context.sendBroadcast(
                            Intent(ACTION_TEMPORARY_ALARM_SOUND_MUTE).apply {
                                setPackage(context.packageName)
                            }
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = !uiState.value.alarmSet && !uiState.value.alarmServiceRunning,
                modifier = Modifier.layoutId("repeatAlarmButton")
            ) {
                IconButton(
                    onClick = { homeViewModel.handleRepeatAlarmClick() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_repeat),
                        tint = MaterialTheme.colorScheme.onSecondary,
                        contentDescription = "Repeat icon",
                        modifier = Modifier.alpha(
                            if (uiState.value.isManualAlarmScheduling) 0.25f else 1f
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.value.alarmServiceRunning && uiState.value.snoozeAvailable,
                modifier = Modifier.layoutId("snoozeButton")
            ) {
                SnoozeButton(
                    onClick = { homeViewModel.handleSnoozeButtonClick(finishableActionSideEffect) },
                    modifier = Modifier.padding(0.dp, MaterialTheme.space.medium, 0.dp, 0.dp)
                )
            }

            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
        }
    }

    CameraPermissionSetAlarmDialog(
        uiState = uiState,
        onPositiveClick = {
            cameraPermissionState.launchPermissionRequest()
            uiState.value = uiState.value.copy(showCameraPermissionDialog = false)
        },
        onNegativeClick = { uiState.value = uiState.value.copy(showCameraPermissionDialog = false) }
    )

    CameraPermissionSetAlarmRevokedDialog(
        uiState = uiState,
        onPositiveClick = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
            uiState.value = uiState.value.copy(showCameraPermissionRevokedDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showCameraPermissionRevokedDialog = false)
        }
    )

    NotificationsPermissionDialog(
        uiState = uiState,
        onPositiveClick = {
            notificationsPermissionState.launchPermissionRequest()
            uiState.value = uiState.value.copy(showNotificationsPermissionDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showNotificationsPermissionDialog = false)
        }
    )

    NotificationsPermissionRevokedDialog(
        uiState = uiState,
        onPositiveClick = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
            uiState.value = uiState.value.copy(showNotificationsPermissionRevokedDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showNotificationsPermissionRevokedDialog = false)
        }
    )

    AlarmPermissionDialog(
        uiState = uiState,
        onPositiveClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startActivity(
                    Intent().apply {
                        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        data = Uri.parse("package:${context.packageName}")
                    }
                )
            }
            uiState.value = uiState.value.copy(showAlarmPermissionDialog = false)
        },
        onNegativeClick = { uiState.value = uiState.value.copy(showAlarmPermissionDialog = false) }
    )

    FullScreenIntentPermissionDialog(
        uiState = uiState,
        onPositiveClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                context.startActivity(
                    Intent().apply {
                        action = Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
                        data = Uri.parse("package:${context.packageName}")
                    }
                )
            }
            uiState.value = uiState.value.copy(showFullScreenIntentPermissionDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showFullScreenIntentPermissionDialog = false)
        }
    )

    if (uiState.value.showCodePossessionConfirmationDialog) {
        CodePossessionConfirmationDialog(
            onDoneClicked = {
                homeViewModel.confirmCodePossession()
                uiState.value = uiState.value.copy(showCodePossessionConfirmationDialog = false)
            },
            onSettingsClicked = {
                homeViewModel.shouldNotUpdateAlarmStateAfterDataStoreManagerUpdate = true
                navController.navigateThrottled(
                    Screen.SettingsScreen.route,
                    lifecycleOwner
                )
                uiState.value = uiState.value.copy(showCodePossessionConfirmationDialog = false)
            },
            onDismissRequest = {
                uiState.value = uiState.value.copy(showCodePossessionConfirmationDialog = false)
            }
        )
    }

    if (uiState.value.showEnableRepeatingAlarmsDialog) {
        EnableRepeatingAlarmsDialog(
            uiState = uiState,
            onPositiveClick = {
                homeViewModel.handleEnableDisableRepeatingAlarms(repeatingAlarmsEnabled = true)
                uiState.value = uiState.value.copy(showEnableRepeatingAlarmsDialog = false)
            },
            onNegativeClick = {
                uiState.value = uiState.value.copy(showEnableRepeatingAlarmsDialog = false)
            }
        )
    }

    if (uiState.value.showDisableRepeatingAlarmsDialog) {
        DisableRepeatingAlarmsDialog(
            uiState = uiState,
            onPositiveClick = {
                homeViewModel.handleEnableDisableRepeatingAlarms(repeatingAlarmsEnabled = false)
                uiState.value = uiState.value.copy(showDisableRepeatingAlarmsDialog = false)
            },
            onNegativeClick = {
                uiState.value = uiState.value.copy(showDisableRepeatingAlarmsDialog = false)
            }
        )
    }
}

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onClickSideEffect: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    IconButton(
        onClick = {
            onClickSideEffect()
            navController.navigateThrottled(Screen.SettingsScreen.route, lifecycleOwner)
        },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_menu),
            contentDescription = "Menu button",
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    uiState: MutableState<HomeUiState>
) {
    AndroidView(
        modifier = modifier,
        factory = {
            QRAlarmTimePicker(it).apply {
                // Right after composing the TimePicker the internal TimePicker(View) calls the
                // timeChangedListener with the current time which breaks the uiState - we have to
                // prevent the uiState update after this initial timeChangedListener call:
                var isInitialUpdate = true

                setOnTimeChangedListener { _, hourOfDay, minute ->
                    if (!isInitialUpdate) {
                        uiState.value.alarmHourOfDay = hourOfDay
                        uiState.value.alarmMinute = minute
                    } else {
                        isInitialUpdate = false
                    }
                }
            }
        },
        update = {
            with(uiState.value) {
                it.setIs24HourView(alarmTimeFormat == TimeFormat.MILITARY)
                it.setTime(alarmHourOfDay, alarmMinute)
                it.isEnabled = !alarmSet && !alarmServiceRunning
            }
        }
    )
}

@Composable
fun StartStopAlarmButton(
    modifier: Modifier = Modifier,
    uiState: MutableState<HomeUiState>,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Text(
            text = stringResource(
                if (uiState.value.alarmSet || uiState.value.alarmServiceRunning) R.string.stop
                else R.string.start
            ),
            fontSize = 26.sp,
            modifier = Modifier.padding(
                MaterialTheme.space.medium,
                MaterialTheme.space.small,
                MaterialTheme.space.medium,
                MaterialTheme.space.extraSmall
            )
        )
    }
}

@Composable
fun SnoozeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.snooze),
            fontSize = 20.sp,
            modifier = Modifier.padding(
                MaterialTheme.space.medium,
                MaterialTheme.space.small,
                MaterialTheme.space.medium,
                MaterialTheme.space.extraSmall
            )
        )
    }
}
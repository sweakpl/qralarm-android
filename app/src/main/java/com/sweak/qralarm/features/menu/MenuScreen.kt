package com.sweak.qralarm.features.menu

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.features.menu.components.AssignDefaultCodeBottomSheet
import com.sweak.qralarm.features.menu.components.DefaultCodeEntry
import com.sweak.qralarm.features.menu.components.MenuEntry
import com.sweak.qralarm.features.menu.components.ThemeToggleEntry

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MenuScreen(
    onBackClicked: () -> Unit,
    onIntroductionClicked: () -> Unit,
    onOptimizationGuideClicked: () -> Unit,
    onEmergencyTaskSettingsClicked: () -> Unit,
    onQRAlarmProClicked: () -> Unit,
    onRateQRAlarmClicked: () -> Unit,
    onScanDefaultCodeClicked: () -> Unit
) {
    val menuViewModel = hiltViewModel<MenuViewModel>()
    val menuScreenState by menuViewModel.state.collectAsStateWithLifecycle()

    var isInTheCameraPermissionFlowForDefaultCodeScan by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )

    OnResume {
        if (isInTheCameraPermissionFlowForDefaultCodeScan) {
            isInTheCameraPermissionFlowForDefaultCodeScan = false

            if (cameraPermissionState.status is PermissionStatus.Granted) {
                onScanDefaultCodeClicked()
            }
        }
    }

    val context = LocalContext.current

    MenuScreenContent(
        state = menuScreenState,
        onEvent = { event ->
            when (event) {
                is MenuScreenUserEvent.OnBackClicked -> onBackClicked()
                is MenuScreenUserEvent.OnIntroductionClicked -> onIntroductionClicked()
                is MenuScreenUserEvent.OnOptimizationGuideClicked -> onOptimizationGuideClicked()
                is MenuScreenUserEvent.OnEmergencyTaskSettingsClicked ->
                    onEmergencyTaskSettingsClicked()
                is MenuScreenUserEvent.OnQRAlarmProClicked -> onQRAlarmProClicked()
                is MenuScreenUserEvent.OnRateQRAlarmClicked -> onRateQRAlarmClicked()
                is MenuScreenUserEvent.TryScanSpecificDefaultCode -> {
                    menuViewModel.onEvent(
                        MenuScreenUserEvent.AssignDefaultCodeDialogVisible(isVisible = false)
                    )

                    if (!cameraPermissionState.status.isGranted) {
                        isInTheCameraPermissionFlowForDefaultCodeScan = true

                        if (cameraPermissionState.status.shouldShowRationale) {
                            menuViewModel.onEvent(
                                MenuScreenUserEvent.CameraPermissionDeniedDialogVisible(
                                    isVisible = true
                                )
                            )
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    } else {
                        onScanDefaultCodeClicked()
                    }
                }
                is MenuScreenUserEvent.GoToApplicationSettingsClicked -> {
                    menuViewModel.onEvent(
                        MenuScreenUserEvent.CameraPermissionDeniedDialogVisible(isVisible = false)
                    )

                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    )
                }
                else -> menuViewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreenContent(
    state: MenuScreenState,
    onEvent: (MenuScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(MenuScreenUserEvent.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.BackArrow,
                            contentDescription =
                            stringResource(R.string.content_description_back_arrow_icon)
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
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
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxWidth()
            ) {
                MenuEntry(
                    title = stringResource(R.string.introduction),
                    onClick = { onEvent(MenuScreenUserEvent.OnIntroductionClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.optimization_guide),
                    onClick = { onEvent(MenuScreenUserEvent.OnOptimizationGuideClicked) }
                )

                DefaultCodeEntry(
                    onClick = {
                        onEvent(
                            MenuScreenUserEvent.AssignDefaultCodeDialogVisible(isVisible = true)
                        )
                    },
                    assignedCode = state.defaultAlarmCode
                )

                MenuEntry(
                    title = stringResource(R.string.emergency_task),
                    onClick = { onEvent(MenuScreenUserEvent.OnEmergencyTaskSettingsClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.qralarm_pro),
                    onClick = { onEvent(MenuScreenUserEvent.OnQRAlarmProClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.rate_qralarm),
                    onClick = { onEvent(MenuScreenUserEvent.OnRateQRAlarmClicked) }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ThemeToggleEntry(
                        theme = state.theme,
                        onThemeToggle = { onEvent(MenuScreenUserEvent.ThemeToggleClicked) }
                    )
                }
            }
        }
    }

    if (state.isAssignDefaultCodeDialogVisible) {
        AssignDefaultCodeBottomSheet(
            onScanCodeClicked = {
                onEvent(MenuScreenUserEvent.TryScanSpecificDefaultCode)
            },
            availableCodes = state.previouslySavedCodes,
            shouldAllowCodeClearance = state.defaultAlarmCode != null,
            onChooseCodeFromList = { chosenCode ->
                onEvent(MenuScreenUserEvent.DefaultCodeChosenFromList(code = chosenCode))
            },
            onDismissRequest = {
                onEvent(MenuScreenUserEvent.AssignDefaultCodeDialogVisible(isVisible = false))
            },
            onClearCodeClicked = {
                onEvent(MenuScreenUserEvent.ClearDefaultAlarmCode)
            }
        )
    }

    if (state.isCameraPermissionDeniedDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.camera_permission_required),
            message = stringResource(R.string.camera_permission_required_description),
            onDismissRequest = {
                onEvent(
                    MenuScreenUserEvent.CameraPermissionDeniedDialogVisible(
                        isVisible = false
                    )
                )
            },
            onPositiveClick = {
                onEvent(MenuScreenUserEvent.GoToApplicationSettingsClicked)
            },
            positiveButtonText = stringResource(R.string.settings),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }
}

@Preview
@Composable
private fun MenuScreenContentPreview() {
    QRAlarmTheme {
        MenuScreenContent(
            state = MenuScreenState(),
            onEvent = {}
        )
    }
}
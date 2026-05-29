package com.sweak.qralarm.features.codes_management

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.components.EditCodeNameBottomSheet
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.features.codes_management.components.DefaultCodeCard
import com.sweak.qralarm.features.codes_management.components.OtherCodesCard

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CodesManagementScreen(
    onBackClicked: () -> Unit,
    onScanDefaultCodeClicked: () -> Unit
) {
    val viewModel = hiltViewModel<CodesManagementViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

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

    CodesManagementScreenContent(
        state = state,
        onEvent = { event ->
            when (event) {
                is CodesManagementScreenUserEvent.OnBackClicked -> onBackClicked()
                is CodesManagementScreenUserEvent.TryScanDefaultCode -> {
                    if (!cameraPermissionState.status.isGranted) {
                        isInTheCameraPermissionFlowForDefaultCodeScan = true

                        if (cameraPermissionState.status.shouldShowRationale) {
                            viewModel.onEvent(
                                CodesManagementScreenUserEvent.CameraPermissionDeniedDialogVisible(
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
                is CodesManagementScreenUserEvent.GoToApplicationSettingsClicked -> {
                    viewModel.onEvent(
                        CodesManagementScreenUserEvent.CameraPermissionDeniedDialogVisible(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodesManagementScreenContent(
    state: CodesManagementScreenState,
    onEvent: (CodesManagementScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.codes_management),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(CodesManagementScreenUserEvent.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.BackArrow,
                            contentDescription =
                                stringResource(R.string.content_description_back_arrow_icon)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = state.isLoading,
            contentAlignment = Alignment.Center,
            label = "codesManagementScreenLoadingAnimation",
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
        ) { isLoading ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(paddingValues = paddingValues)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(MaterialTheme.space.xLarge)
                            .align(Alignment.Center)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.small),
                    modifier = Modifier
                        .padding(paddingValues = paddingValues)
                        .padding(bottom = MaterialTheme.space.mediumLarge)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.codes_management_description),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(
                            start = MaterialTheme.space.medium,
                            top = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.mediumLarge,
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.Star,
                            contentDescription = stringResource(R.string.content_description_star_icon),
                            modifier = Modifier.padding(end = MaterialTheme.space.small)
                        )
                        Text(
                            text = stringResource(R.string.default_alarm_code),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    DefaultCodeCard(
                        defaultCode = state.defaultAlarmCode,
                        isPickingDefault = state.isPickingDefault,
                        hasOtherCodes = state.otherCodes.isNotEmpty(),
                        onScanClicked = { onEvent(CodesManagementScreenUserEvent.TryScanDefaultCode) },
                        onPickExistingClicked = {
                            onEvent(CodesManagementScreenUserEvent.PickExistingCodeAsDefaultClicked)
                        },
                        onClearClicked = {
                            onEvent(CodesManagementScreenUserEvent.ClearDefaultCodeClicked)
                        },
                        onEditClicked = { code ->
                            onEvent(CodesManagementScreenUserEvent.EditCodeNameClicked(code))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.space.medium)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.space.medium)
                    ) {
                        Text(
                            text = stringResource(
                                if (state.isPickingDefault) R.string.pick_default_code
                                else R.string.other_codes
                            ),
                            style = MaterialTheme.typography.labelLarge
                        )

                        TextButton(
                            onClick = {
                                onEvent(CodesManagementScreenUserEvent.CancelPickingCodeClicked)
                            },
                            enabled = state.isPickingDefault,
                            modifier = Modifier.visible(state.isPickingDefault)
                        ) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    }

                    OtherCodesCard(
                        codes = state.otherCodes,
                        isPickingDefault = state.isPickingDefault,
                        onEditClicked = { code ->
                            onEvent(CodesManagementScreenUserEvent.EditCodeNameClicked(code))
                        },
                        onCodePickedAsDefault = { code ->
                            onEvent(CodesManagementScreenUserEvent.CodePickedAsDefault(code))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.space.medium)
                    )
                }
            }
        }
    }

    if (state.codeBeingEdited != null) {
        EditCodeNameBottomSheet(
            initialCodeName = state.codeBeingEdited.name,
            onConfirmClicked = { newName ->
                onEvent(CodesManagementScreenUserEvent.CodeNameEditConfirmed(newName))
            },
            onDismissRequest = {
                onEvent(CodesManagementScreenUserEvent.CodeNameEditDismissed)
            }
        )
    }

    if (state.isCameraPermissionDeniedDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.camera_permission_required),
            message = stringResource(R.string.camera_permission_required_description),
            onDismissRequest = {
                onEvent(
                    CodesManagementScreenUserEvent.CameraPermissionDeniedDialogVisible(
                        isVisible = false
                    )
                )
            },
            onPositiveClick = {
                onEvent(CodesManagementScreenUserEvent.GoToApplicationSettingsClicked)
            },
            positiveButtonText = stringResource(R.string.settings),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }
}

@Preview
@Composable
private fun CodesManagementScreenContentPreview() {
    QRAlarmTheme {
        CodesManagementScreenContent(
            state = CodesManagementScreenState(
                isLoading = false,
                defaultAlarmCode = Code(
                    id = 1,
                    value = "1324678965131687461",
                    name = "Coffee bag code"
                ),
                otherCodes = listOf(
                    Code(id = 2, value = "fjsdlauo44938cym9x3,4", name = "Front door"),
                    Code(id = 3, value = "www.examplewebsite.com", name = "Bedroom safe"),
                    Code(id = 4, value = "132467896513168746133")
                )
            ),
            onEvent = {}
        )
    }
}

package com.sweak.qralarm.features.disable_alarm_scanner

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.ui.components.code_scanner.compose.CodeScanner
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.features.disable_alarm_scanner.components.Toast

@Composable
fun DisableAlarmScannerScreen(
    idOfAlarm: Long,
    isDisablingBeforeAlarmFired: Boolean,
    onAlarmDisabled: (uriStringToTryToOpen: String?) -> Unit,
    onCloseClicked: () -> Unit
) {
    val disableAlarmScannerViewModel =
        hiltViewModel<DisableAlarmScannerViewModel, DisableAlarmScannerViewModel.Factory> { factory ->
            factory.create(
                idOfAlarm = idOfAlarm,
                isDisablingBeforeAlarmFired = isDisablingBeforeAlarmFired
            )
        }
    val disableAlarmScannerScreenState by disableAlarmScannerViewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    ObserveAsEvents(
        flow = disableAlarmScannerViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is DisableAlarmScannerScreenBackendEvent.ChallengeCompleted -> {
                    onAlarmDisabled(event.uriStringToOpen)
                }

                is DisableAlarmScannerScreenBackendEvent.CameraInitializationError -> {
                    Toast.makeText(
                        context,
                        R.string.failed_to_initialize_camera,
                        Toast.LENGTH_LONG
                    ).show()
                    onCloseClicked()
                }
            }
        }
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            disableAlarmScannerViewModel.initializeCamera(
                DisableAlarmScannerScreenUserEvent.InitializeCamera(
                    appContext = context,
                    lifecycleOwner = lifecycleOwner,
                    windowInfo = windowInfo
                )
            )
        }
    }

    DisableAlarmScannerScreenContent(
        state = disableAlarmScannerScreenState,
        onEvent = { event ->
            when (event) {
                is DisableAlarmScannerScreenUserEvent.OnCloseClicked -> {
                    onCloseClicked()
                }

                else -> disableAlarmScannerViewModel.onEvent(event)
            }
        }
    )
}

@Composable
fun DisableAlarmScannerScreenContent(
    state: DisableAlarmScannerScreenState,
    onEvent: (DisableAlarmScannerScreenUserEvent) -> Unit
) {
    Scaffold(containerColor = Color.White) { paddingValues ->
        CodeScanner(
            surfaceRequest = state.surfaceRequest,
            isFlashEnabled = state.isFlashEnabled,
            codeName = state.codeName,
            objectName = state.objectName,
            isCustomObject = state.isCustomObject,
            customObjectsShown = state.customObjectsShown,
            customObjectsTotal = state.customObjectsTotal,
            detectionBox = state.detectionBox,
            confirmationProgress = state.confirmationProgress,
            recognitionError = state.recognitionError,
            onCloseClicked = { onEvent(DisableAlarmScannerScreenUserEvent.OnCloseClicked) },
            onToggleFlash = { onEvent(DisableAlarmScannerScreenUserEvent.ToggleFlash) },
            paddingValues = paddingValues
        )
    }

    AnimatedVisibility(
        visible = state.shouldShowIncorrectCodeWarning,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Toast(stringResource(R.string.incorrect_code_scanned))
    }
}

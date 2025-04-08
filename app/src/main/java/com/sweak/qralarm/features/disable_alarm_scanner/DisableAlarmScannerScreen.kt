package com.sweak.qralarm.features.disable_alarm_scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.ui.components.code_scanner.QRAlarmCodeScanner
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.features.disable_alarm_scanner.components.Toast

@Composable
fun DisableAlarmScannerScreen(
    onAlarmDisabled: (uriStringToTryToOpen: String?) -> Unit,
    onCloseClicked: () -> Unit
) {
    val disableAlarmScannerViewModel = hiltViewModel<DisableAlarmScannerViewModel>()
    val disableAlarmScannerScreenState by disableAlarmScannerViewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(
        flow = disableAlarmScannerViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned -> {
                    onAlarmDisabled(event.uriStringToOpen)
                }
            }
        }
    )

    DisableAlarmScannerScreenContent(
        state = disableAlarmScannerScreenState,
        onEvent = { event ->
            when (event) {
                is DisableAlarmScannerScreenUserEvent.CodeResultScanned -> {
                    disableAlarmScannerViewModel.onEvent(event)
                }
                is DisableAlarmScannerScreenUserEvent.OnCloseClicked -> {
                    onCloseClicked()
                }
            }
        }
    )
}

@Composable
fun DisableAlarmScannerScreenContent(
    state: DisableAlarmScannerScreenState,
    onEvent: (DisableAlarmScannerScreenUserEvent) -> Unit
) {
    QRAlarmCodeScanner(
        decodeCallback = { codeValue ->
            onEvent(DisableAlarmScannerScreenUserEvent.CodeResultScanned(codeResult = codeValue))
        },
        closeCallback = {
            onEvent(DisableAlarmScannerScreenUserEvent.OnCloseClicked)
        }
    )

    AnimatedVisibility(
        visible = state.shouldShowIncorrectCodeWarning,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Toast(stringResource(R.string.incorrect_code_scanned))
    }
}
package com.sweak.qralarm.features.disable_alarm_scanner

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sweak.qralarm.core.ui.components.code_scanner.QRAlarmCodeScanner
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents

@Composable
fun DisableAlarmScannerScreen(
    onAlarmDisabled: (uriStringToTryToOpen: String?) -> Unit,
    onCloseClicked: () -> Unit
) {
    val disableAlarmScannerViewModel = hiltViewModel<DisableAlarmScannerViewModel>()

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
    onEvent: (DisableAlarmScannerScreenUserEvent) -> Unit
) {
    QRAlarmCodeScanner(
        decodeCallback = { result ->
            onEvent(DisableAlarmScannerScreenUserEvent.CodeResultScanned(result = result))
        },
        closeCallback = {
            onEvent(DisableAlarmScannerScreenUserEvent.OnCloseClicked)
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    )
}
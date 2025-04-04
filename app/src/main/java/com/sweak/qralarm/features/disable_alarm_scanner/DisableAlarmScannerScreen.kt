package com.sweak.qralarm.features.disable_alarm_scanner

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.sweak.qralarm.R
import com.sweak.qralarm.core.ui.components.code_scanner.QRAlarmCodeScanner
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents

@Composable
fun DisableAlarmScannerScreen(
    onAlarmDisabled: (uriStringToTryToOpen: String?) -> Unit,
    onCloseClicked: () -> Unit
) {
    val disableAlarmScannerViewModel = hiltViewModel<DisableAlarmScannerViewModel>()

    val context = LocalContext.current

    ObserveAsEvents(
        flow = disableAlarmScannerViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned -> {
                    onAlarmDisabled(event.uriStringToOpen)
                }
                is DisableAlarmScannerScreenBackendEvent.IncorrectCodeScanned -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.incorrect_code_scanned),
                        Toast.LENGTH_SHORT
                    ).show()
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
        }
    )
}
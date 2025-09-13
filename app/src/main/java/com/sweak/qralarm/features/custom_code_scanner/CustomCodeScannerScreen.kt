package com.sweak.qralarm.features.custom_code_scanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.ui.components.code_scanner.QRAlarmCodeScanner
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents

@Composable
fun CustomCodeScannerScreen(
    onCustomCodeSaved: () -> Unit,
    onCloseClicked: () -> Unit
) {
    val customCodeScannerViewModel = hiltViewModel<CustomCodeScannerViewModel>()

    ObserveAsEvents(
        flow = customCodeScannerViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                CustomCodeScannerScreenBackendEvent.CustomCodeSaved -> onCustomCodeSaved()
            }
        }
    )

    CustomCodeScannerScreenContent(
        onEvent = { event ->
            when (event) {
                is CustomCodeScannerScreenUserEvent.CodeResultScanned -> {
                    customCodeScannerViewModel.onEvent(event)
                }
                is CustomCodeScannerScreenUserEvent.OnCloseClicked -> {
                    onCloseClicked()
                }
            }
        }
    )
}

@Composable
private fun CustomCodeScannerScreenContent(
    onEvent: (CustomCodeScannerScreenUserEvent) -> Unit
) {
    QRAlarmCodeScanner(
        decodeCallback = { result ->
            onEvent(CustomCodeScannerScreenUserEvent.CodeResultScanned(result = result))
        },
        closeCallback = {
            onEvent(CustomCodeScannerScreenUserEvent.OnCloseClicked)
        }
    )
}

@Preview
@Composable
private fun CustomCodeScannerScreenContentPreview() {
    QRAlarmTheme {
        CustomCodeScannerScreenContent(
            onEvent = {}
        )
    }
}
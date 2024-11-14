package com.sweak.qralarm.features.custom_code_scanner

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.ui.components.code_scanner.QRAlarmCodeScanner
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents

@Composable
fun CustomCodeScannerScreen(onCustomCodeSaved: () -> Unit) {
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
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
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
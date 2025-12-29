package com.sweak.qralarm.features.custom_code_scanner

import android.widget.Toast
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.ui.components.code_scanner.compose.CodeScanner
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.R

@Composable
fun CustomCodeScannerScreen(
    onCustomCodeSaved: () -> Unit,
    onCloseClicked: () -> Unit
) {
    val customCodeScannerViewModel = hiltViewModel<CustomCodeScannerViewModel>()
    val customCodeScannerScreenState by customCodeScannerViewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    ObserveAsEvents(
        flow = customCodeScannerViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                CustomCodeScannerScreenBackendEvent.CustomCodeSaved -> onCustomCodeSaved()
                CustomCodeScannerScreenBackendEvent.CameraInitializationError -> {
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
        customCodeScannerViewModel.onEvent(
            CustomCodeScannerScreenUserEvent.InitializeCamera(
                appContext = context,
                lifecycleOwner = lifecycleOwner,
                windowInfo = windowInfo
            )
        )
    }

    CustomCodeScannerScreenContent(
        state = customCodeScannerScreenState,
        onEvent = { event ->
            when (event) {
                is CustomCodeScannerScreenUserEvent.CloseClicked -> {
                    onCloseClicked()
                }
                else -> customCodeScannerViewModel.onEvent(event)
            }
        }
    )
}

@Composable
fun CustomCodeScannerScreenContent(
    state: CustomCodeScannerScreenState,
    onEvent: (CustomCodeScannerScreenUserEvent) -> Unit
) {
    Scaffold(containerColor = Color.White) { paddingValues ->
        CodeScanner(
            surfaceRequest = state.surfaceRequest,
            isFlashEnabled = state.isFlashEnabled,
            onCloseClicked = { onEvent(CustomCodeScannerScreenUserEvent.CloseClicked) },
            onToggleFlash = { onEvent(CustomCodeScannerScreenUserEvent.ToggleFlash) },
            paddingValues = paddingValues
        )
    }
}

@Preview
@Composable
private fun CustomCodeScannerScreenContentPreview() {
    QRAlarmTheme {
        CustomCodeScannerScreenContent(
            state = CustomCodeScannerScreenState(),
            onEvent = {}
        )
    }
}
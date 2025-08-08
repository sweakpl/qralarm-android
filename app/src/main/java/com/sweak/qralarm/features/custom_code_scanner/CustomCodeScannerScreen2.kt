package com.sweak.qralarm.features.custom_code_scanner

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents

@Composable
fun CustomCodeScannerScreen2(
    onCustomCodeSaved: () -> Unit,
    onCloseClicked: () -> Unit
) {
    val customCodeScannerViewModel2 = hiltViewModel<CustomCodeScannerViewModel2>()
    val customCodeScannerScreenState2 by customCodeScannerViewModel2.state.collectAsStateWithLifecycle()

    ObserveAsEvents(
        flow = customCodeScannerViewModel2.backendEvents,
        onEvent = { event ->
            when (event) {
                CustomCodeScannerScreenBackendEvent2.CustomCodeSaved -> onCustomCodeSaved()
            }
        }
    )

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        customCodeScannerViewModel2.onEvent(
            CustomCodeScannerScreenUserEvent2.BindToCamera(
                appContext = context,
                lifecycleOwner = lifecycleOwner
            )
        )
    }

    CustomCodeScannerScreenContent2(
        state = customCodeScannerScreenState2
    )
}

@Composable
fun CustomCodeScannerScreenContent2(
    state: CustomCodeScannerScreenState2
) {
    state.surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request
        )
    }
}
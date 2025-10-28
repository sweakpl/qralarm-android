package com.sweak.qralarm.features.custom_code_scanner

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.features.custom_code_scanner.components.ScanOverlay

@Composable
fun CustomCodeScannerScreen(
    onCustomCodeSaved: () -> Unit,
    onCloseClicked: () -> Unit
) {
    val customCodeScannerViewModel = hiltViewModel<CustomCodeScannerViewModel>()
    val customCodeScannerScreenState by customCodeScannerViewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(
        flow = customCodeScannerViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                CustomCodeScannerScreenBackendEvent.CustomCodeSaved -> onCustomCodeSaved()
            }
        }
    )

    val context = LocalContext.current
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
        Box {
            state.surfaceRequest?.let { request ->
                CameraXViewfinder(
                    implementationMode = ImplementationMode.EMBEDDED,
                    surfaceRequest = request,
                    modifier = Modifier.fillMaxSize()
                )
            }

            ScanOverlay()

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) +
                                MaterialTheme.space.medium,
                        top = paddingValues.calculateTopPadding() + MaterialTheme.space.medium,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) +
                                MaterialTheme.space.medium
                    )
            ) {
                IconButton(onClick = { onEvent(CustomCodeScannerScreenUserEvent.CloseClicked) }) {
                    Icon(
                        imageVector = QRAlarmIcons.Close,
                        contentDescription = stringResource(R.string.content_description_close_icon),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(MaterialTheme.space.large)
                    )
                }

                IconButton(onClick = { onEvent(CustomCodeScannerScreenUserEvent.ToggleFlash) }) {
                    Icon(
                        imageVector =
                            if (state.isFlashEnabled) QRAlarmIcons.FlashOff
                            else QRAlarmIcons.FlashOn,
                        contentDescription = stringResource(R.string.content_description_flash_icon),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(MaterialTheme.space.large)
                    )
                }
            }
        }
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
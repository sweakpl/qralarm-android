package com.sweak.qralarm.features.disable_alarm_scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.ButterflyBush
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents

@Composable
fun DisableAlarmScannerScreen(onAlarmDisabled: () -> Unit) {
    val disableAlarmScannerViewModel = hiltViewModel<DisableAlarmScannerViewModel>()

    ObserveAsEvents(
        flow = disableAlarmScannerViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned -> onAlarmDisabled()
            }
        }
    )

    DisableAlarmScannerScreenContent(
        onEvent = { event ->
            when (event) {
                is DisableAlarmScannerScreenUserEvent.CodeResultScanned -> {
                    disableAlarmScannerViewModel.onEvent(event)
                }
            }
        }
    )
}

@Composable
fun DisableAlarmScannerScreenContent(
    onEvent: (DisableAlarmScannerScreenUserEvent) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    lateinit var codeScanner: CodeScanner

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                codeScanner.releaseResources()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                codeScanner.startPreview()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { context ->
            val codeScannerView = CodeScannerView(context).apply {
                maskColor = BlueZodiac.copy(alpha = 0.5F).toArgb()
                frameColor = ButterflyBush.toArgb()
                autoFocusButtonColor = ButterflyBush.toArgb()
                flashButtonColor = ButterflyBush.toArgb()
            }

            codeScanner = CodeScanner(context, codeScannerView).apply {
                scanMode = ScanMode.CONTINUOUS
                isTouchFocusEnabled = true
                errorCallback = ErrorCallback.SUPPRESS

                decodeCallback = DecodeCallback { result ->
                    onEvent(DisableAlarmScannerScreenUserEvent.CodeResultScanned(result = result))
                }

                startPreview()
            }

            codeScannerView
        }
    )
}
package com.sweak.qralarm.features.custom_code_scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
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
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
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
                scanMode = ScanMode.SINGLE
                isTouchFocusEnabled = true
                errorCallback = ErrorCallback.SUPPRESS

                decodeCallback = DecodeCallback { result ->
                    onEvent(CustomCodeScannerScreenUserEvent.CodeResultScanned(result = result))
                }

                startPreview()
            }

            codeScannerView
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
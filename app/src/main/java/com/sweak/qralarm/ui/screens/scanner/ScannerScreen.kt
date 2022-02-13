package com.sweak.qralarm.ui.screens.scanner

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.budiyev.android.codescanner.*
import com.google.zxing.BarcodeFormat

@Composable
fun ScannerScreen() {
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
            val codeScannerView = CodeScannerView(context)

            codeScanner = CodeScanner(context, codeScannerView).apply {
                formats = listOf(BarcodeFormat.QR_CODE)
                scanMode = ScanMode.CONTINUOUS
                isTouchFocusEnabled = true
                decodeCallback = DecodeCallback { result ->
                    Log.i("ScannerScreen", "Code is: ${result.text}")
                }
                errorCallback = ErrorCallback.SUPPRESS
                startPreview()
            }

            codeScannerView
        }
    )
}
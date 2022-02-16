package com.sweak.qralarm.ui.screens.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.budiyev.android.codescanner.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.sweak.qralarm.ui.screens.shared.viewmodels.AlarmViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalPermissionsApi
@Composable
fun ScannerScreen(
    navController: NavHostController,
    alarmViewModel: AlarmViewModel
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
            val codeScannerView = CodeScannerView(context)

            codeScanner = CodeScanner(context, codeScannerView).apply {
                formats = listOf(BarcodeFormat.QR_CODE)
                scanMode = ScanMode.CONTINUOUS
                isTouchFocusEnabled = true
                decodeCallback = DecodeCallback { result ->
                    handleDecodeResult(result, navController, alarmViewModel)
                }
                errorCallback = ErrorCallback.SUPPRESS
                startPreview()
            }

            codeScannerView
        }
    )
}

@ExperimentalPermissionsApi
fun handleDecodeResult(
    result: Result,
    navController: NavHostController,
    alarmViewModel: AlarmViewModel
) {
    if (result.text == "StopAlarm") {
        alarmViewModel.stopAlarm()
        CoroutineScope(Dispatchers.Main).launch {
            navController.popBackStack()
        }
    }
}
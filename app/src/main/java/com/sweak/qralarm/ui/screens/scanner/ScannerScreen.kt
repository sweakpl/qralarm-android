package com.sweak.qralarm.ui.screens.scanner

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.BarcodeFormat
import com.sweak.qralarm.ui.screens.components.DismissCodeAddedDialog
import com.sweak.qralarm.ui.screens.popBackStackThrottled
import com.sweak.qralarm.ui.theme.BlueZodiac
import com.sweak.qralarm.ui.theme.ButterflyBush

@Composable
fun ScannerScreen(
    navController: NavHostController,
    scannerViewModel: ScannerViewModel,
    acceptAnyCodeType: Boolean,
    scannerMode: String?,
    finishableActionSideEffect: () -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val uiState = remember { scannerViewModel.scannerUiState }

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
                formats =
                    if (acceptAnyCodeType) CodeScanner.ALL_FORMATS
                    else listOf(BarcodeFormat.QR_CODE)
                scanMode = ScanMode.CONTINUOUS
                isTouchFocusEnabled = true
                decodeCallback = DecodeCallback { result ->
                    scannerViewModel.handleDecodeResult(
                        result = result,
                        scannerMode = scannerMode,
                        navController = navController,
                        cancelAlarmSideEffect = finishableActionSideEffect
                    )
                }
                errorCallback = ErrorCallback.SUPPRESS
                startPreview()
            }

            codeScannerView
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    )

    DismissCodeAddedDialog(
        uiState = uiState,
        onPositiveClick = {
            uiState.value = uiState.value.copy(
                showDismissCodeAddedDialog = false,
                hasNewDismissCodeBeenAccepted = true
            )
            navController.popBackStackThrottled(lifecycleOwner)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showDismissCodeAddedDialog = false)
        }
    )
}
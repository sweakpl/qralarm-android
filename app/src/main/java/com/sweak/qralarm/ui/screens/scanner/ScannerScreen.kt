package com.sweak.qralarm.ui.screens.scanner

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.budiyev.android.codescanner.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.sweak.qralarm.ui.screens.settings.SettingsViewModel
import com.sweak.qralarm.ui.screens.shared.viewmodels.AlarmViewModel
import com.sweak.qralarm.ui.theme.BlueZodiac
import com.sweak.qralarm.ui.theme.ButterflyBush
import com.sweak.qralarm.util.SCAN_MODE_DISMISS_ALARM
import com.sweak.qralarm.util.SCAN_MODE_SET_CUSTOM_CODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalPagerApi
@InternalCoroutinesApi
@ExperimentalPermissionsApi
@Composable
fun ScannerScreen(
    navController: NavHostController,
    alarmViewModel: AlarmViewModel,
    settingsViewModel: SettingsViewModel,
    acceptAnyCodeType: Boolean,
    scannerMode: String?,
    finishableActionSideEffect: () -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
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
                    handleDecodeResult(
                        result = result,
                        scannerMode = scannerMode,
                        navController = navController,
                        alarmViewModel = alarmViewModel,
                        settingsViewModel = settingsViewModel,
                        cancelAlarmSideEffect = finishableActionSideEffect
                    )
                }
                errorCallback = ErrorCallback.SUPPRESS
                startPreview()
            }

            codeScannerView
        }
    )
}

@ExperimentalMaterialApi
@ExperimentalPagerApi
@InternalCoroutinesApi
@ExperimentalPermissionsApi
fun handleDecodeResult(
    result: Result,
    scannerMode: String?,
    navController: NavHostController,
    alarmViewModel: AlarmViewModel,
    settingsViewModel: SettingsViewModel,
    cancelAlarmSideEffect: () -> Unit
) {
    if (scannerMode == SCAN_MODE_DISMISS_ALARM) {
        if (result.text == alarmViewModel.getDismissCode()) {
            val stopAlarmJob = alarmViewModel.stopAlarm()

            CoroutineScope(Dispatchers.Main).launch {
                stopAlarmJob.join()
                cancelAlarmSideEffect.invoke()
                navController.popBackStack()
            }
        }
    } else if (scannerMode == SCAN_MODE_SET_CUSTOM_CODE) {
        settingsViewModel.setCustomQRCode(result.text)
        CoroutineScope(Dispatchers.Main).launch {
            navController.popBackStack()
        }
    }
}
package com.sweak.qralarm.ui.screens.scanner

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.zxing.Result
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.screens.popBackStackThrottled
import com.sweak.qralarm.util.DEFAULT_DISMISS_ALARM_CODE
import com.sweak.qralarm.util.SCAN_MODE_DISMISS_ALARM
import com.sweak.qralarm.util.SCAN_MODE_SET_CUSTOM_CODE
import com.sweak.qralarm.util.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val qrAlarmManager: QRAlarmManager
) : ViewModel() {

    val scannerUiState = mutableStateOf(ScannerUiState())

    fun handleDecodeResult(
        result: Result,
        scannerMode: String?,
        navController: NavHostController,
        lifecycleOwner: LifecycleOwner,
        cancelAlarmSideEffect: () -> Unit
    ) {
        if (scannerMode == SCAN_MODE_DISMISS_ALARM) {
            if (result.text in getDismissCodes()) {
                val stopAlarmJob = stopAlarm()

                CoroutineScope(Dispatchers.Main).launch {
                    stopAlarmJob.join()
                    cancelAlarmSideEffect.invoke()
                    navController.popBackStackThrottled(
                        Screen.HomeScreen.route,
                        false,
                        lifecycleOwner
                    )
                }
            }
        } else if (scannerMode == SCAN_MODE_SET_CUSTOM_CODE) {
            if (!scannerUiState.value.showDismissCodeAddedDialog) {
                setCustomQRCode(result.text)
            }
        }
    }

    private fun getDismissCodes(): List<String> {
        val userSavedDismissCode = runBlocking {
            dataStoreManager.getString(DataStoreManager.DISMISS_ALARM_CODE).first()
        }

        return setOf(userSavedDismissCode, DEFAULT_DISMISS_ALARM_CODE).toList()
    }

    private fun stopAlarm(): Job {
        val stopAlarmJob = viewModelScope.launch {
            qrAlarmManager.cancelAlarm()

            dataStoreManager.apply {
                putBoolean(DataStoreManager.ALARM_SET, false)
                putBoolean(DataStoreManager.ALARM_SNOOZED, false)
            }
        }

        return stopAlarmJob
    }

    private fun setCustomQRCode(code: String) {
        viewModelScope.launch {
            dataStoreManager.putString(DataStoreManager.DISMISS_ALARM_CODE, code)
            scannerUiState.value = scannerUiState.value.copy(
                showDismissCodeAddedDialog = true,
                newDismissAlarmCode = code
            )
        }
    }
}
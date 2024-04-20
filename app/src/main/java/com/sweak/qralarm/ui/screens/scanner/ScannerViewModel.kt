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
import com.sweak.qralarm.util.ALARM_TYPE_NORMAL
import com.sweak.qralarm.util.DEFAULT_DISMISS_ALARM_CODE
import com.sweak.qralarm.util.SCAN_MODE_DISMISS_ALARM
import com.sweak.qralarm.util.SCAN_MODE_SET_CUSTOM_CODE
import com.sweak.qralarm.util.Screen
import com.sweak.qralarm.util.currentTimeInMillis
import com.sweak.qralarm.util.getAlarmHourOfDay
import com.sweak.qralarm.util.getAlarmMinute
import com.sweak.qralarm.util.getAlarmTimeInMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val qrAlarmManager: QRAlarmManager
) : ViewModel() {

    val scannerUiState = mutableStateOf(ScannerUiState())
    private var shouldAllowFurtherDecodeResultHandling = true

    fun handleDecodeResult(
        result: Result,
        scannerMode: String?,
        navController: NavHostController,
        lifecycleOwner: LifecycleOwner,
        cancelAlarmSideEffect: () -> Unit
    ) {
        if (scannerMode == SCAN_MODE_DISMISS_ALARM) {
            if (result.text in getDismissCodes() && shouldAllowFurtherDecodeResultHandling) {
                shouldAllowFurtherDecodeResultHandling = false

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
        return try {
            val userSavedDismissCode = runBlocking {
                dataStoreManager.getString(DataStoreManager.DISMISS_ALARM_CODE).first()
            }

            setOf(userSavedDismissCode, DEFAULT_DISMISS_ALARM_CODE).toList()
        } catch (interruptedException: InterruptedException) {
            emptyList()
        }
    }

    private fun stopAlarm() = viewModelScope.launch {
        qrAlarmManager.cancelAlarm()

        dataStoreManager.apply {
            putBoolean(DataStoreManager.ALARM_SET, false)
            putBoolean(DataStoreManager.ALARM_SNOOZED, false)
        }

        val isManualAlarmScheduling =
            dataStoreManager.getBoolean(DataStoreManager.MANUAL_ALARM_SCHEDULING).first()
        val alarmTime = dataStoreManager.getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()
        val currentTime = currentTimeInMillis()
        val shouldRepeatAlarm = !isManualAlarmScheduling && alarmTime < currentTime

        if (shouldRepeatAlarm) {
            val newAlarmTime = getAlarmTimeInMillis(
                getAlarmHourOfDay(alarmTime),
                getAlarmMinute(alarmTime)
            )
            val alarmTimeZoneId = ZoneId.systemDefault().id

            qrAlarmManager.setAlarm(newAlarmTime, ALARM_TYPE_NORMAL)

            dataStoreManager.apply {
                putBoolean(DataStoreManager.ALARM_SET, true)
                putBoolean(DataStoreManager.ALARM_SNOOZED, false)

                putLong(DataStoreManager.ALARM_TIME_IN_MILLIS, newAlarmTime)
                putString(DataStoreManager.ALARM_TIME_ZONE_ID, alarmTimeZoneId)

                putInt(
                    DataStoreManager.SNOOZE_AVAILABLE_COUNT,
                    getInt(DataStoreManager.SNOOZE_MAX_COUNT).first()
                )
            }
        }
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
package com.sweak.qralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.widget.Toast
import com.sweak.qralarm.R
import com.sweak.qralarm.data.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CancelAlarmReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var qrAlarmManager: QRAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        receiverScope.launch {
            val alarmTimeInMillis =
                dataStoreManager.getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()
            val currentTimeInMillis = System.currentTimeMillis()

            val isNoCodeAlarmCancellationAllowed = dataStoreManager.getBoolean(
                DataStoreManager.ALLOW_NO_CODE_ALARM_CANCEL
            ).first()

            // If cancellation request was at least an hour before the alarm - turn off immediately:
            if (isNoCodeAlarmCancellationAllowed && alarmTimeInMillis - currentTimeInMillis > 3600000) {
                qrAlarmManager.cancelAlarm()

                dataStoreManager.putBoolean(DataStoreManager.ALARM_SET, false)
            } else { // ... else tell the user to manually disable the alarm
                Handler(context.mainLooper).post {
                    Toast.makeText(
                        context,
                        context.getString(R.string.alarm_in_less_than_1h_cancel_by_scanning),
                        Toast.LENGTH_LONG
                    ).show()
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                }
            }
        }
    }
}
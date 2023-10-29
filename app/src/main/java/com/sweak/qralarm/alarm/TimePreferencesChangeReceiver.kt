package com.sweak.qralarm.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.sweak.qralarm.MainActivity
import com.sweak.qralarm.R
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.theme.Jacarta
import com.sweak.qralarm.util.ALARM_MISSED_NOTIFICATION_ID
import com.sweak.qralarm.util.ALARM_MISSED_NOTIFICATION_REQUEST_CODE
import com.sweak.qralarm.util.ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.util.ALARM_TYPE_NORMAL
import com.sweak.qralarm.util.currentTimeInMillis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.zone.ZoneRulesException
import javax.inject.Inject

@AndroidEntryPoint
class TimePreferencesChangeReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var qrAlarmManager: QRAlarmManager

    @Inject
    lateinit var notificationManager: NotificationManager

    private val intentActionsToFilter = listOf(
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_DATE_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in intentActionsToFilter) receiverScope.launch {
            val isNormalAlarmSet = dataStoreManager.getBoolean(DataStoreManager.ALARM_SET).first()
            val isSnoozeAlarmSet = dataStoreManager.getBoolean(DataStoreManager.ALARM_SNOOZED).first()
            val isAlarmSetInternal = qrAlarmManager.isAlarmSet()

            if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
                // The general point of handling the time zone change is to adjust the alarm time
                // and, if necessary, reschedule it. E.g. the user had a preset alarm time for 9:00,
                // they travel somewhere else and they expect that the alarm in the app will still
                // be displayed as 9:00. We're using timestamps, so the default behavior in this
                // case would be that the displayed alarm would have changed for the user. E.g.
                // 9:00 at GMT+0:00 would be translated to 11:00 at Europe/Warsaw+2:00 - in this
                // case the timestamp has to have 2 hours subtracted to be displayed as 9:00.

                val newZoneIdString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    intent.extras?.getString(Intent.EXTRA_TIMEZONE)
                } else ZoneId.systemDefault().id
                val oldZoneIdString =
                    dataStoreManager.getString(DataStoreManager.ALARM_TIME_ZONE_ID).first()

                newZoneIdString?.let {
                    // Always save new time zone id:
                    dataStoreManager.putString(DataStoreManager.ALARM_TIME_ZONE_ID, newZoneIdString)
                }

                val newZoneId = getZoneIdOf(newZoneIdString)
                val oldZoneId = getZoneIdOf(oldZoneIdString)
                if (newZoneId == null || oldZoneId == null) return@launch

                val alarmSecondsOffset = getAlarmSecondsOffsetForTimezones(oldZoneId, newZoneId)
                // If the time zone change didn't change time, leave the alarm as is:
                if (alarmSecondsOffset == 0) return@launch

                val oldAlarmTimInMillis =
                    dataStoreManager.getLong(DataStoreManager.ALARM_TIME_IN_MILLIS).first()
                val newAlarmTimeInMillis = oldAlarmTimInMillis + (alarmSecondsOffset * 1000)
                // Always save the new alarm time:
                dataStoreManager
                    .putLong(DataStoreManager.ALARM_TIME_IN_MILLIS, newAlarmTimeInMillis)

                if (isNormalAlarmSet && !isSnoozeAlarmSet) {
                    if (currentTimeInMillis() > newAlarmTimeInMillis) {
                        // If the normal alarm is in the past after the change - cancel it:
                        cancelAlarmAndShowAlarmMissedNotification(context)
                    } else {
                        // If the normal alarm is still in the future - reschedule it:
                        qrAlarmManager.cancelAlarm()
                        qrAlarmManager.setAlarm(newAlarmTimeInMillis, ALARM_TYPE_NORMAL)
                    }
                } else if (isSnoozeAlarmSet) {
                    // If the alarm was snoozed - always cancel the alarm. We don't want to
                    // accidentally make e.g. a 10 minutes snooze a 1 hour and 10 minutes snooze:
                    cancelAlarmAndShowAlarmMissedNotification(context)
                }
            } else {
                // The general point of handling the time and date change is to make sure that the
                // scheduled alarm time is not accidentally in the past - in those cases the alarm
                // is cancelled. If the alarm is still in the future it is left as is.

                // If there is no alarm set then just abort:
                if (!isAlarmSetInternal) return@launch

                if (isNormalAlarmSet && !isSnoozeAlarmSet) {
                    val alarmTimeInMillis = dataStoreManager.getLong(
                        DataStoreManager.ALARM_TIME_IN_MILLIS
                    ).first()

                    // Cancelling only if the normal alarm time is in the past:
                    if (currentTimeInMillis() > alarmTimeInMillis) {
                        cancelAlarmAndShowAlarmMissedNotification(context)
                    }
                } else if (isSnoozeAlarmSet) {
                    val alarmTimeInMillis = dataStoreManager.getLong(
                        DataStoreManager.SNOOZE_ALARM_TIME_IN_MILLIS
                    ).first()

                    // Cancelling only if the snoozed alarm time is in the past:
                    if (currentTimeInMillis() > alarmTimeInMillis) {
                        cancelAlarmAndShowAlarmMissedNotification(context)
                    }
                }
            }
        }
    }

    private fun getZoneIdOf(zoneIdString: String?): ZoneId? {
        return try {
            zoneIdString?.let { ZoneId.of(it) }
        } catch (exception: DateTimeException) {
            null
        } catch (exception: ZoneRulesException) {
            null
        }
    }

    private fun getAlarmSecondsOffsetForTimezones(oldAlarmZoneId: ZoneId, newAlarmZoneId: ZoneId): Int {
        val oldZoneOffset = oldAlarmZoneId.rules.getOffset(Instant.now())
        val newZoneOffset = newAlarmZoneId.rules.getOffset(Instant.now())

        return oldZoneOffset.totalSeconds - newZoneOffset.totalSeconds
    }

    private suspend fun cancelAlarmAndShowAlarmMissedNotification(context: Context) {
        qrAlarmManager.cancelAlarm()

        with(dataStoreManager) {
            putBoolean(DataStoreManager.ALARM_SET, false)
            putBoolean(DataStoreManager.ALARM_SNOOZED, false)
        }

        showAlarmMissedNotification(context)
    }

    private fun showAlarmMissedNotification(context: Context) {
        val alarmMissedPendingIntent = PendingIntent.getActivity(
            context,
            ALARM_MISSED_NOTIFICATION_REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
        )

        val alarmSetIndicationNotification = NotificationCompat.Builder(
            context,
            ALARM_SET_INDICATION_NOTIFICATION_CHANNEL_ID
        ).apply {
            color = Jacarta.toArgb()
            priority = NotificationCompat.PRIORITY_LOW
            setOngoing(false)
            setAutoCancel(true)
            setColorized(true)
            setContentTitle(context.getString(R.string.alarm_missed_notification_title))
            setContentText(context.getString(R.string.alarm_missed_notification_text))
            setSmallIcon(R.drawable.ic_notification_icon)
            setContentIntent(alarmMissedPendingIntent)
        }.build()

        notificationManager.notify(
            ALARM_MISSED_NOTIFICATION_ID,
            alarmSetIndicationNotification
        )
    }
}
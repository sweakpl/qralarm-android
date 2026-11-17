package com.sweak.qralarm.alarm.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.ALARM_NOTIFICATION_CHANNEL_ID
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmNotificationFactory @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    @SuppressLint("FullScreenIntentPolicy")
    fun create(alarmId: Long): Notification {
        val alarmActivityPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            Intent(context, AlarmActivity::class.java).apply {
                putExtra(AlarmActivity.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmActivity.EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY, false)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, ALARM_NOTIFICATION_CHANNEL_ID)
            .setColor(Jacarta.toArgb())
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setColorized(true)
            .setContentTitle(context.getString(R.string.alarm_notification_title))
            .setSmallIcon(R.drawable.ic_qralarm)
            .setContentIntent(alarmActivityPendingIntent)
            .setFullScreenIntent(alarmActivityPendingIntent, true)
            .build()
    }
}

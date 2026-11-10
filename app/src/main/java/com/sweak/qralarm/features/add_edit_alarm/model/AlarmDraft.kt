package com.sweak.qralarm.features.add_edit_alarm.model

import android.net.Uri
import android.os.Parcelable
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.domain.alarm.DEFAULT_CANCEL_LOCK_DURATION_IN_MINUTES
import com.sweak.qralarm.core.domain.alarm.DEFAULT_GENTLE_WAKE_UP_DURATION_IN_SECONDS
import com.sweak.qralarm.core.domain.alarm.DEFAULT_SNOOZE_NUMBER_TO_DURATION_PAIR
import com.sweak.qralarm.core.domain.alarm.DEFAULT_TEMPORARY_MUTE_DURATION_IN_SECONDS
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.Code
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlarmDraft(
    val alarmHourOfDay: Int? = null,
    val alarmMinute: Int? = null,
    val onlyOnceAlarmDateInMillis: Long = 0L,
    val isOnlyOnceAlarmDateSticky: Boolean = true,
    val alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper =
        AlarmRepeatingScheduleWrapper(),
    val snoozeNumberToDurationPair: Pair<Int, Int> = DEFAULT_SNOOZE_NUMBER_TO_DURATION_PAIR,
    val ringtone: Ringtone = Ringtone.GENTLE_GUITAR,
    val currentCustomAlarmRingtoneUri: Uri? = null,
    val temporaryCustomAlarmRingtoneUri: Uri? = null,
    val alarmVolumePercentage: Int? = null,
    val areVibrationsEnabled: Boolean = true,
    val isCodeEnabled: Boolean = true,
    val currentlyAssignedCode: Code? = null,
    val temporaryAssignedCode: Code? = null,
    val isOpenCodeLinkEnabled: Boolean = false,
    val cancelLockDurationInMinutes: Int = DEFAULT_CANCEL_LOCK_DURATION_IN_MINUTES,
    val isEmergencyTaskEnabled: Boolean = true,
    val alarmLabel: String? = null,
    val gentleWakeupDurationInSeconds: Int = DEFAULT_GENTLE_WAKE_UP_DURATION_IN_SECONDS,
    val temporaryMuteDurationInSeconds: Int = DEFAULT_TEMPORARY_MUTE_DURATION_IN_SECONDS
) : Parcelable

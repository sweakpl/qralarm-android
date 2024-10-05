package com.sweak.qralarm.features.add_edit_alarm.model

import androidx.annotation.RawRes
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.alarm.AlarmRingtone

sealed class AlarmRingtoneWrapper {
    sealed class OriginalRingtone(
        val alarmRingtone: AlarmRingtone,
        @RawRes val ringtoneResource: Int
    ) : AlarmRingtoneWrapper() {
        data object GentleGuitar : OriginalRingtone(
            alarmRingtone = AlarmRingtone.GENTLE_GUITAR,
            ringtoneResource = R.raw.gentle_guitar
        )
        data object AlarmClock : OriginalRingtone(
            alarmRingtone = AlarmRingtone.ALARM_CLOCK,
            ringtoneResource = R.raw.alarm_clock
        )
        data object AirHorn : OriginalRingtone(
            alarmRingtone = AlarmRingtone.AIR_HORN,
            ringtoneResource = R.raw.air_horn
        )
    }

    data class CustomRingtone(
        val customSoundUriString: String? = null
    ) : AlarmRingtoneWrapper()
}

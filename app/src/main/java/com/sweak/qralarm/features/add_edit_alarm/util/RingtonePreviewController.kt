package com.sweak.qralarm.features.add_edit_alarm.util

import android.net.Uri
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class RingtonePreviewController @Inject constructor(
    private val alarmRingtonePlayer: AlarmRingtonePlayer
) {
    private val _playingRingtone = MutableStateFlow<Ringtone?>(null)
    val playingRingtone: StateFlow<Ringtone?> = _playingRingtone.asStateFlow()

    fun getAvailableRingtones(): List<Ringtone> {
        val isSystemDefaultAvailable = alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable()

        return Ringtone.entries.filter { it != Ringtone.SYSTEM_DEFAULT || isSystemDefaultAvailable }
    }

    fun toggle(ringtone: Ringtone, customRingtoneUri: Uri?, onPlaybackError: () -> Unit) {
        if (_playingRingtone.value == ringtone) {
            stop()
            return
        }

        _playingRingtone.value = ringtone

        val onPreviewCompleted: (Boolean) -> Unit = { hasErrorOccurred ->
            _playingRingtone.value = null
            if (hasErrorOccurred) onPlaybackError()
        }

        if (ringtone == Ringtone.CUSTOM_SOUND && customRingtoneUri != null) {
            alarmRingtonePlayer.playAlarmRingtonePreview(
                alarmRingtoneUri = customRingtoneUri,
                onPreviewCompleted = onPreviewCompleted
            )
        } else {
            alarmRingtonePlayer.playAlarmRingtonePreview(
                ringtone = ringtone,
                onPreviewCompleted = onPreviewCompleted
            )
        }
    }

    fun stop() {
        alarmRingtonePlayer.stop()
        _playingRingtone.value = null
    }

    fun release() {
        alarmRingtonePlayer.onDestroy()
    }
}

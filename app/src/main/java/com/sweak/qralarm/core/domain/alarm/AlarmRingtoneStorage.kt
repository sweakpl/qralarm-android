package com.sweak.qralarm.core.domain.alarm

import java.io.InputStream

interface AlarmRingtoneStorage {
    /**
     * Copies the picker-returned content:// URI into app storage for [alarmId]. Returns the
     * resulting file:// URI string. Throws IOException/SecurityException/NullPointerException
     * on failure.
     */
    fun saveContentUriForAlarm(contentUriString: String, alarmId: Long): String

    /**
     * Copies the existing ringtone file for [sourceAlarmId] to a new file for [newAlarmId].
     * Returns the new file:// URI string, or null if the source file does not exist (or the
     * copy failed). Never throws.
     */
    fun duplicateForAlarm(sourceAlarmId: Long, newAlarmId: Long): String?

    /**
     * Opens the ringtone file for [alarmId] for reading. Returns null if there is no ringtone file
     * for [alarmId] or it could not be opened. The caller owns the returned stream and must close
     * it. Never throws.
     */
    fun openForAlarm(alarmId: Long): InputStream?

    /**
     * Writes [inputStream] as the ringtone file for [alarmId], replacing any file already stored
     * for it. Returns the resulting file:// URI string, or null if the write failed. Does not
     * close [inputStream]. Never throws.
     */
    fun writeForAlarm(alarmId: Long, inputStream: InputStream): String?

    /** Deletes the ringtone file for [alarmId] if present. No-op if it doesn't exist. */
    fun deleteForAlarm(alarmId: Long)

    /** True if a ringtone file exists for [alarmId]. */
    fun exists(alarmId: Long): Boolean

    /**
     * Makes sure the ringtone file for [alarmId] is stored where it can be read before the first
     * unlock after a reboot, moving it from the legacy location if needed. Returns the current
     * file:// URI string, or null if there is no ringtone file for [alarmId] or the move failed.
     * Never throws.
     */
    fun migrateToDeviceProtectedStorage(alarmId: Long): String?
}

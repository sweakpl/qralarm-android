package com.sweak.qralarm.core.domain.alarm

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

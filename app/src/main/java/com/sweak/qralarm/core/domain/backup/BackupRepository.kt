package com.sweak.qralarm.core.domain.backup

interface BackupRepository {

    suspend fun readAlarmsForBackup(): List<BackupAlarm>
    suspend fun readCodesForBackup(): List<BackupCode>

    suspend fun getAllAlarmIds(): List<Long>

    /**
     * Discards every stored alarm and code and puts [codes] and [alarms] in their place. All or
     * nothing: if this fails, what was already stored is left as it was.
     *
     * Restored alarms land disabled and with no custom ringtone assigned - the caller restores
     * the ringtone files afterward.
     *
     * Returns the old-to-new id mappings, needed to place the restored ringtone files and to
     * remap the default alarm code.
     */
    suspend fun replaceAll(codes: List<BackupCode>, alarms: List<BackupAlarm>): RestoreIdMappings
}

data class RestoreIdMappings(
    val alarmIdMap: Map<Long, Long>,
    val codeIdMap: Map<Long, Long>
)

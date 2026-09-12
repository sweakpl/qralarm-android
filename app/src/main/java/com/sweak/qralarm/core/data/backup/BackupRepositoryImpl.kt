package com.sweak.qralarm.core.data.backup

import androidx.room.withTransaction
import com.sweak.qralarm.core.domain.backup.BackupAlarm
import com.sweak.qralarm.core.domain.backup.BackupCode
import com.sweak.qralarm.core.domain.backup.BackupRepository
import com.sweak.qralarm.core.domain.backup.RestoreIdMappings
import com.sweak.qralarm.core.storage.database.QRAlarmDatabase
import com.sweak.qralarm.core.storage.database.dao.AlarmsDao
import com.sweak.qralarm.core.storage.database.dao.CodesDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val qrAlarmDatabase: QRAlarmDatabase,
    private val alarmsDao: AlarmsDao,
    private val codesDao: CodesDao
) : BackupRepository {

    override suspend fun readAlarmsForBackup(): List<BackupAlarm> =
        alarmsDao.getAllAlarms().first().map { it.toBackupAlarm() }

    override suspend fun readCodesForBackup(): List<BackupCode> =
        codesDao.getAllCodes().first().map { it.toBackupCode() }

    override suspend fun getAllAlarmIds(): List<Long> = alarmsDao.getAllAlarmIds()

    override suspend fun replaceAll(
        codes: List<BackupCode>,
        alarms: List<BackupAlarm>
    ): RestoreIdMappings = qrAlarmDatabase.withTransaction {
        alarmsDao.deleteAllAlarms()
        codesDao.deleteAllCodes()

        // Codes go in first - alarm has a foreign key on code.codeId.
        val codeIdMap = mutableMapOf<Long, Long>()

        codes.forEach { backupCode ->
            val newCodeId = codesDao.insertCode(code = backupCode.toCodeEntity())

            // insertCode ignores conflicts on the unique code.value index and returns -1 for them.
            // Leaving such an id out of the map makes the alarms that referenced it restore with
            // no assigned code, instead of failing the whole transaction on the foreign key.
            if (newCodeId != -1L) {
                codeIdMap[backupCode.codeId] = newCodeId
            }
        }

        val alarmIdMap = mutableMapOf<Long, Long>()

        alarms.forEach { backupAlarm ->
            val newAlarmId = alarmsDao.upsertAlarm(
                alarmEntity = backupAlarm.toAlarmEntity(
                    assignedCodeId = backupAlarm.assignedCodeId?.let { codeIdMap[it] }
                )
            )

            alarmIdMap[backupAlarm.alarmId] = newAlarmId
        }

        RestoreIdMappings(alarmIdMap = alarmIdMap, codeIdMap = codeIdMap)
    }
}

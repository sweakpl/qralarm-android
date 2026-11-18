package com.sweak.qralarm.core.data.backup

import androidx.room.withTransaction
import com.sweak.qralarm.core.domain.backup.BackupAlarm
import com.sweak.qralarm.core.data.custom_object.CustomObjectFileStore
import com.sweak.qralarm.core.domain.backup.BackupCode
import com.sweak.qralarm.core.domain.backup.BackupCustomObject
import com.sweak.qralarm.core.storage.database.dao.CustomObjectsDao
import com.sweak.qralarm.core.storage.database.model.CustomObjectEntity
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
    private val codesDao: CodesDao,
    private val customObjectsDao: CustomObjectsDao,
    private val customObjectFiles: CustomObjectFileStore
) : BackupRepository {

    override suspend fun readCustomObjectsForBackup(): List<BackupCustomObject> =
        customObjectsDao.getAllObjects().first().map {
            BackupCustomObject(
                objectId = it.objectId,
                name = it.name,
                createdAtMillis = it.createdAtMillis,
                embeddingThreshold = it.embeddingThreshold,
                referenceCount = it.referenceCount,
                files = customObjectFiles.readAllFiles(it.objectId)
            )
        }

    override suspend fun readAlarmsForBackup(): List<BackupAlarm> =
        alarmsDao.getAllAlarms().first().map { it.toBackupAlarm() }

    override suspend fun readCodesForBackup(): List<BackupCode> =
        codesDao.getAllCodes().first().map { it.toBackupCode() }

    override suspend fun getAllAlarmIds(): List<Long> = alarmsDao.getAllAlarmIds()

    override suspend fun replaceAll(
        codes: List<BackupCode>,
        alarms: List<BackupAlarm>,
        customObjects: List<BackupCustomObject>
    ): RestoreIdMappings = qrAlarmDatabase.withTransaction {
        alarmsDao.deleteAllAlarms()
        codesDao.deleteAllCodes()
        customObjectsDao.deleteAllObjectRows()

        val customObjectIdMap = mutableMapOf<Long, Long>()
        customObjects.forEach { backupObject ->
            customObjectIdMap[backupObject.objectId] = customObjectsDao.insertObject(
                CustomObjectEntity(
                    name = backupObject.name,
                    createdAtMillis = backupObject.createdAtMillis,
                    referenceCount = backupObject.referenceCount,
                    embeddingThreshold = backupObject.embeddingThreshold
                )
            )
        }

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
                    assignedCodeId = backupAlarm.assignedCodeId?.let { codeIdMap[it] },
                    customObjectId = backupAlarm.customObjectId?.let { customObjectIdMap[it] }
                )
            )

            alarmIdMap[backupAlarm.alarmId] = newAlarmId
        }

        // The rows are committed with the transaction; the files follow right after it.
        customObjectFiles.deleteAll()
        customObjects.forEach { backupObject ->
            customObjectIdMap[backupObject.objectId]?.let { customObjectFiles.writeAllFiles(it, backupObject.files) }
        }

        RestoreIdMappings(alarmIdMap = alarmIdMap, codeIdMap = codeIdMap)
    }
}

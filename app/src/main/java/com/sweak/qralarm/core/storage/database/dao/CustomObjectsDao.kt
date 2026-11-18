package com.sweak.qralarm.core.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sweak.qralarm.core.storage.database.model.AlarmCustomObjectEntity
import com.sweak.qralarm.core.storage.database.model.CustomObjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomObjectsDao {

    @Insert
    suspend fun insertObject(customObject: CustomObjectEntity): Long

    @Update
    suspend fun updateObject(customObject: CustomObjectEntity)

    @Query("SELECT * FROM custom_object WHERE objectId = :objectId")
    suspend fun getObject(objectId: Long): CustomObjectEntity?

    @Query("SELECT * FROM custom_object ORDER BY name COLLATE NOCASE")
    fun getAllObjects(): Flow<List<CustomObjectEntity>>

    @Query("SELECT COUNT(*) FROM alarm_custom_object WHERE objectId = :objectId")
    suspend fun countAlarmsUsingObject(objectId: Long): Int

    /**
     * The object is removed from every alarm that used it; an alarm left without any item falls
     * back to the stop button instead of becoming impossible to dismiss.
     */
    @Transaction
    suspend fun deleteObject(objectId: Long) {
        deleteAlarmLinks(objectId)
        deleteObjectRow(objectId)
        resetAlarmsWithoutObjects()
    }

    @Query("DELETE FROM alarm_custom_object WHERE objectId = :objectId")
    suspend fun deleteAlarmLinks(objectId: Long)

    @Query("DELETE FROM custom_object WHERE objectId = :objectId")
    suspend fun deleteObjectRow(objectId: Long)

    @Query(
        "UPDATE alarm SET dismissalMethod = 'NONE' WHERE dismissalMethod = 'CUSTOM_OBJECT' " +
            "AND alarmId NOT IN (SELECT alarmId FROM alarm_custom_object)"
    )
    suspend fun resetAlarmsWithoutObjects()

    @Query("SELECT objectId FROM alarm_custom_object WHERE alarmId = :alarmId ORDER BY position")
    suspend fun getObjectIdsForAlarm(alarmId: Long): List<Long>

    @Query("DELETE FROM alarm_custom_object WHERE alarmId = :alarmId")
    suspend fun deleteLinksForAlarm(alarmId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLinks(links: List<AlarmCustomObjectEntity>)

    @Transaction
    suspend fun setObjectsForAlarm(alarmId: Long, objectIds: List<Long>) {
        deleteLinksForAlarm(alarmId)
        insertLinks(objectIds.distinct().mapIndexed { i, id -> AlarmCustomObjectEntity(alarmId, id, i) })
    }

    @Query("DELETE FROM custom_object")
    suspend fun deleteAllObjectRows()
}

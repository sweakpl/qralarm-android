package com.sweak.qralarm.core.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

    @Query("SELECT COUNT(*) FROM alarm WHERE customObjectId = :objectId")
    suspend fun countAlarmsUsingObject(objectId: Long): Int

    /** Alarms that used the object fall back to the stop button instead of becoming invalid. */
    @Transaction
    suspend fun deleteObject(objectId: Long) {
        resetAlarmsUsingObject(objectId)
        deleteObjectRow(objectId)
    }

    @Query(
        "UPDATE alarm SET dismissalMethod = 'NONE', customObjectId = NULL " +
            "WHERE customObjectId = :objectId"
    )
    suspend fun resetAlarmsUsingObject(objectId: Long)

    @Query("DELETE FROM custom_object WHERE objectId = :objectId")
    suspend fun deleteObjectRow(objectId: Long)

    @Query("UPDATE alarm SET dismissalMethod = 'NONE', customObjectId = NULL WHERE customObjectId IS NOT NULL")
    suspend fun resetAllAlarmsUsingObjects()

    @Query("DELETE FROM custom_object")
    suspend fun deleteAllObjectRows()
}

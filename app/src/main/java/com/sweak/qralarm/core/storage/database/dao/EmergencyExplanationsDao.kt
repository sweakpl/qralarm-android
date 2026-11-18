package com.sweak.qralarm.core.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sweak.qralarm.core.storage.database.model.EmergencyExplanationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyExplanationsDao {

    @Insert
    suspend fun insertExplanation(explanation: EmergencyExplanationEntity): Long

    @Query("SELECT * FROM emergency_explanation ORDER BY createdAtMillis DESC, explanationId DESC")
    fun getAllExplanations(): Flow<List<EmergencyExplanationEntity>>

    @Query("DELETE FROM emergency_explanation WHERE explanationId = :explanationId")
    suspend fun deleteExplanation(explanationId: Long)

    @Query("DELETE FROM emergency_explanation")
    suspend fun deleteAllExplanations()
}

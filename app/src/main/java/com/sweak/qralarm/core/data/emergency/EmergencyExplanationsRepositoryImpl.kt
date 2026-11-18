package com.sweak.qralarm.core.data.emergency

import com.sweak.qralarm.core.domain.emergency.EmergencyExplanation
import com.sweak.qralarm.core.domain.emergency.EmergencyExplanationsRepository
import com.sweak.qralarm.core.storage.database.dao.EmergencyExplanationsDao
import com.sweak.qralarm.core.storage.database.model.EmergencyExplanationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmergencyExplanationsRepositoryImpl @Inject constructor(
    private val dao: EmergencyExplanationsDao
) : EmergencyExplanationsRepository {

    override suspend fun addExplanation(explanation: EmergencyExplanation) {
        dao.insertExplanation(
            EmergencyExplanationEntity(
                alarmId = explanation.alarmId,
                alarmLabel = explanation.alarmLabel,
                alarmHourOfDay = explanation.alarmHourOfDay,
                alarmMinute = explanation.alarmMinute,
                dismissalSummary = explanation.dismissalSummary,
                createdAtMillis = explanation.createdAtMillis,
                text = explanation.text
            )
        )
    }

    override fun getAllExplanations(): Flow<List<EmergencyExplanation>> =
        dao.getAllExplanations().map { entities ->
            entities.map {
                EmergencyExplanation(
                    explanationId = it.explanationId,
                    alarmId = it.alarmId,
                    alarmLabel = it.alarmLabel,
                    alarmHourOfDay = it.alarmHourOfDay,
                    alarmMinute = it.alarmMinute,
                    dismissalSummary = it.dismissalSummary,
                    createdAtMillis = it.createdAtMillis,
                    text = it.text
                )
            }
        }

    override suspend fun deleteExplanation(explanationId: Long) =
        dao.deleteExplanation(explanationId)

    override suspend fun deleteAllExplanations() = dao.deleteAllExplanations()
}

package com.sweak.qralarm.core.domain.emergency

import kotlinx.coroutines.flow.Flow

data class EmergencyExplanation(
    val explanationId: Long = 0,
    val alarmId: Long?,
    val alarmLabel: String?,
    val alarmHourOfDay: Int?,
    val alarmMinute: Int?,
    /** What the alarm normally required, e.g. "Vitamin D bottle" or "QR code". */
    val dismissalSummary: String?,
    val createdAtMillis: Long,
    val text: String
)

interface EmergencyExplanationsRepository {
    suspend fun addExplanation(explanation: EmergencyExplanation)
    fun getAllExplanations(): Flow<List<EmergencyExplanation>>
    suspend fun deleteExplanation(explanationId: Long)
    suspend fun deleteAllExplanations()
}

package com.sweak.qralarm.core.domain.alarm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class AddOrEditAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val codesRepository: CodesRepository,
    private val alarmRingtoneStorage: AlarmRingtoneStorage
) {
    suspend operator fun invoke(
        alarm: Alarm,
        temporaryCustomRingtoneContentUriString: String? = null
    ): Result {
        val resolvedAlarm = alarm.copy(
            assignedCode = resolveCode(alarm.assignedCode)
        )
        val upsertedAlarmId = alarmsRepository.addOrEditAlarm(resolvedAlarm)
        val alarmId = if (upsertedAlarmId > 0) upsertedAlarmId else alarm.alarmId

        codesRepository.cleanupUnreferencedCodes()

        if (temporaryCustomRingtoneContentUriString == null) {
            return Result.Success(alarmId = alarmId)
        }

        return try {
            val savedRingtoneUri = withContext(Dispatchers.IO) {
                alarmRingtoneStorage.saveContentUriForAlarm(
                    contentUriString = temporaryCustomRingtoneContentUriString,
                    alarmId = alarmId
                )
            }

            alarmsRepository.setAlarmRingtoneUri(alarmId = alarmId, uri = savedRingtoneUri)

            Result.Success(alarmId = alarmId)
        } catch (exception: Exception) {
            if (exception is IOException ||
                exception is SecurityException ||
                exception is NullPointerException
            ) {
                Result.CustomRingtoneSaveFailed(alarmId = alarmId)
            } else {
                throw exception
            }
        }
    }

    private suspend fun resolveCode(code: Code?): Code? {
        if (code == null) return null

        val id = if (code.codeId != 0L) {
            code.codeId
        } else {
            codesRepository.findOrCreateCode(code.value)
        }

        val stored = codesRepository.getCode(id)

        if (stored?.name != code.name) {
            codesRepository.updateCodeName(id, code.name)
        }

        return code.copy(codeId = id)
    }

    sealed class Result {
        abstract val alarmId: Long

        data class Success(override val alarmId: Long) : Result()
        data class CustomRingtoneSaveFailed(override val alarmId: Long) : Result()
    }
}

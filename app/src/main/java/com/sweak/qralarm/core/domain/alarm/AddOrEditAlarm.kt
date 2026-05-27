package com.sweak.qralarm.core.domain.alarm

import javax.inject.Inject

class AddOrEditAlarm @Inject constructor(
    private val alarmsRepository: AlarmsRepository,
    private val codesRepository: CodesRepository
) {
    suspend operator fun invoke(alarm: Alarm): Long {
        val resolvedAlarm = alarm.copy(
            assignedCode = resolveCode(alarm.assignedCode)
        )
        val alarmId = alarmsRepository.addOrEditAlarm(resolvedAlarm)

        codesRepository.cleanupUnreferencedCodes()

        return alarmId
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
}

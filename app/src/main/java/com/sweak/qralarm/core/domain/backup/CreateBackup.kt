package com.sweak.qralarm.core.domain.backup

import com.sweak.qralarm.core.domain.alarm.AlarmRingtoneStorage
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.util.Result
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class CreateBackup @Inject constructor(
    private val backupRepository: BackupRepository,
    private val codesRepository: CodesRepository,
    private val userDataRepository: UserDataRepository,
    private val alarmRingtoneStorage: AlarmRingtoneStorage,
    private val backupCodec: BackupCodec
) {
    suspend operator fun invoke(destinationUriString: String): Result<Unit, BackupError> {
        val bundle = BackupBundle(
            alarms = backupRepository.readAlarmsForBackup(),
            codes = backupRepository.readCodesForBackup(),
            preferences = BackupPreferences(
                defaultAlarmCodeId = codesRepository.getDefaultAlarmCodeFlow().first()?.codeId,
                emergencySliderRange = userDataRepository.emergencySliderRange.first(),
                emergencyRequiredMatches = userDataRepository.emergencyRequiredMatches.first(),
                emergencyDisableRepeatingAlarms = userDataRepository.isEmergencyDisableRepeatingAlarmsEnabled.first(),
                theme = userDataRepository.theme.first()
            )
        )

        return try {
            backupCodec.write(
                bundle = bundle,
                destinationUriString = destinationUriString,
                openRingtone = { alarmId -> alarmRingtoneStorage.openForAlarm(alarmId = alarmId) }
            )

            Result.Success(Unit)
        } catch (_: IOException) {
            Result.Error(BackupError.IoFailure)
        } catch (_: SecurityException) {
            // The place the user picked can stop being writable between picking it and writing.
            Result.Error(BackupError.IoFailure)
        }
    }
}

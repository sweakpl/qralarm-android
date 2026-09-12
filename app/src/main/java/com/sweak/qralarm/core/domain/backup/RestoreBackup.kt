package com.sweak.qralarm.core.domain.backup

import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmRingtoneStorage
import com.sweak.qralarm.core.domain.alarm.AlarmScheduler
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.util.Result
import java.io.IOException
import javax.inject.Inject

/**
 * Puts everything a backup holds in place of what is currently stored - the alarms, the codes and
 * the settings the backup carries are all replaced, not merged.
 *
 * The backup is read in full before anything is touched, so a file that turns out not to be a
 * usable backup leaves everything as it was.
 *
 * Restored alarms are left switched off.
 */
class RestoreBackup @Inject constructor(
    private val backupRepository: BackupRepository,
    private val alarmsRepository: AlarmsRepository,
    private val codesRepository: CodesRepository,
    private val userDataRepository: UserDataRepository,
    private val alarmRingtoneStorage: AlarmRingtoneStorage,
    private val alarmScheduler: AlarmScheduler,
    private val backupCodec: BackupCodec
) {
    suspend operator fun invoke(sourceUriString: String): Result<Unit, BackupError> {
        val backupReader = try {
            backupCodec.open(sourceUriString = sourceUriString)
        } catch (_: BackupFormatException.NotAQRAlarmBackup) {
            return Result.Error(BackupError.NotAQRAlarmBackup)
        } catch (unsupportedFormatVersion: BackupFormatException.UnsupportedFormatVersion) {
            return Result.Error(
                BackupError.UnsupportedFormatVersion(
                    fileFormatVersion = unsupportedFormatVersion.fileFormatVersion,
                    supportedFormatVersion = CURRENT_BACKUP_FORMAT_VERSION
                )
            )
        } catch (_: IOException) {
            return Result.Error(BackupError.IoFailure)
        } catch (_: SecurityException) {
            // The file the user picked can stop being readable between picking it and reading it.
            return Result.Error(BackupError.IoFailure)
        }

        backupReader.use { reader ->
            val replacedAlarmIds = backupRepository.getAllAlarmIds()

            replacedAlarmIds.forEach { alarmId ->
                alarmScheduler.cancelAlarm(alarmId = alarmId)
            }

            codesRepository.setDefaultAlarmCodeById(codeId = null)

            val restoreIdMappings = backupRepository.replaceAll(
                codes = reader.bundle.codes,
                alarms = reader.bundle.alarms
            )

            restoreRingtones(
                alarms = reader.bundle.alarms,
                backupReader = reader,
                alarmIdMap = restoreIdMappings.alarmIdMap
            )

            restorePreferences(
                preferences = reader.bundle.preferences,
                codeIdMap = restoreIdMappings.codeIdMap
            )

            replacedAlarmIds.forEach { alarmId ->
                alarmRingtoneStorage.deleteForAlarm(alarmId = alarmId)
            }
        }

        return Result.Success(Unit)
    }

    private suspend fun restoreRingtones(
        alarms: List<BackupAlarm>,
        backupReader: BackupReader,
        alarmIdMap: Map<Long, Long>
    ) {
        alarms.filter { it.hasCustomRingtoneFile }.forEach { backupAlarm ->
            val restoredAlarmId = alarmIdMap[backupAlarm.alarmId] ?: return@forEach

            val restoredRingtoneUriString =
                backupReader.openRingtone(oldAlarmId = backupAlarm.alarmId)?.use { ringtoneStream ->
                    alarmRingtoneStorage.writeForAlarm(
                        alarmId = restoredAlarmId,
                        inputStream = ringtoneStream
                    )
                }

            if (restoredRingtoneUriString != null) {
                alarmsRepository.setAlarmRingtoneUri(
                    alarmId = restoredAlarmId,
                    uri = restoredRingtoneUriString
                )
            } else {
                // Fallback to default ringtone if custom one happened to be missing:
                alarmsRepository.setAlarmRingtoneUri(alarmId = restoredAlarmId, uri = null)
                alarmsRepository.setAlarmRingtone(
                    alarmId = restoredAlarmId,
                    ringtone = Alarm.Ringtone.GENTLE_GUITAR
                )
            }
        }
    }

    private suspend fun restorePreferences(
        preferences: BackupPreferences,
        codeIdMap: Map<Long, Long>
    ) {
        preferences.emergencySliderRange?.let { emergencySliderRange ->
            userDataRepository.setEmergencySliderRange(range = emergencySliderRange)
        }

        preferences.emergencyRequiredMatches?.let { emergencyRequiredMatches ->
            userDataRepository.setEmergencyRequiredMatches(matches = emergencyRequiredMatches)
        }

        preferences.theme?.let { theme ->
            userDataRepository.setTheme(theme = theme)
        }

        // Setting the default code also clears out the codes nothing points at any more, so the
        // restored alarms have to already be in place by the time this runs.
        codesRepository.setDefaultAlarmCodeById(
            codeId = preferences.defaultAlarmCodeId?.let { codeIdMap[it] }
        )
    }
}

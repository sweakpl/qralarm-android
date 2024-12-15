package com.sweak.qralarm.core.data.user

import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.storage.datastore.QRAlarmPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val qrAlarmPreferencesDataSource: QRAlarmPreferencesDataSource
): UserDataRepository {

    override suspend fun setIntroductionFinished(finished: Boolean) {
        qrAlarmPreferencesDataSource.setIntroductionFinished(finished = finished)
    }

    override val isIntroductionFinished: Flow<Boolean>
        get() = qrAlarmPreferencesDataSource.getIntroductionFinished()

    override suspend fun setOptimizationGuideState(
        state: UserDataRepository.OptimizationGuideState
    ) {
        qrAlarmPreferencesDataSource.setOptimizationGuideState(state = state.name)
    }

    override val optimizationGuideState: Flow<UserDataRepository.OptimizationGuideState>
        get() = qrAlarmPreferencesDataSource.getOptimizationGuideState().map {
            UserDataRepository.OptimizationGuideState.valueOf(it)
        }

    override suspend fun setTemporaryScannedCode(code: String?) {
        qrAlarmPreferencesDataSource.setTemporaryScannedCode(code = code)
    }

    override val temporaryScannedCode: Flow<String?>
        get() = qrAlarmPreferencesDataSource.getTemporaryScannedCode()

    override suspend fun setAlarmMissedDetected(detected: Boolean) {
        qrAlarmPreferencesDataSource.setAlarmMissedDetected(detected = detected)
    }

    override val isAlarmMissedDetected: Flow<Boolean>
        get() = qrAlarmPreferencesDataSource.getAlarmMissedDetected()

    override suspend fun setNextRatePromptTimeInMillis(promptTime: Long?) {
        qrAlarmPreferencesDataSource.setNextRatePromptTimeInMillis(promptTime = promptTime)
    }

    override val nextRatePromptTimeInMillis: Flow<Long?>
        get() = qrAlarmPreferencesDataSource.getNextRatePromptTimeInMillis()

    override suspend fun setLegacyDataMigrated(migrated: Boolean) {
        qrAlarmPreferencesDataSource.setLegacyDataMigrated(migrated = migrated)
    }

    override val isLegacyDataMigrated: Flow<Boolean>
        get() = qrAlarmPreferencesDataSource.getLegacyDataMigrated()
}
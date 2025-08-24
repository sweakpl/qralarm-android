package com.sweak.qralarm.core.data.user

import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.storage.datastore.QRAlarmPreferencesDataSource
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_DEFAULT_REQUIRED_MATCHES
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_DEFAULT_SLIDER_RANGE
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
        get() = qrAlarmPreferencesDataSource.getIntroductionFinished().map {
            it == true
        }

    override suspend fun setOptimizationGuideState(
        state: UserDataRepository.OptimizationGuideState
    ) {
        qrAlarmPreferencesDataSource.setOptimizationGuideState(state = state.name)
    }

    override val optimizationGuideState: Flow<UserDataRepository.OptimizationGuideState>
        get() = qrAlarmPreferencesDataSource.getOptimizationGuideState().map { stateString ->
            stateString?.let {
                UserDataRepository.OptimizationGuideState.valueOf(it)
            } ?: UserDataRepository.OptimizationGuideState.NONE
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
        get() = qrAlarmPreferencesDataSource.getAlarmMissedDetected().map {
            it == true
        }

    override suspend fun setNextRatePromptTimeInMillis(promptTime: Long) {
        qrAlarmPreferencesDataSource.setNextRatePromptTimeInMillis(promptTime = promptTime)
    }

    override val nextRatePromptTimeInMillis: Flow<Long?>
        get() = qrAlarmPreferencesDataSource.getNextRatePromptTimeInMillis()

    override suspend fun setDefaultAlarmCode(code: String?) {
        qrAlarmPreferencesDataSource.setDefaultAlarmCode(code = code)
    }

    override val defaultAlarmCode: Flow<String?>
        get() = qrAlarmPreferencesDataSource.getDefaultAlarmCode()

    override suspend fun setEmergencySliderRange(range: IntRange) {
        qrAlarmPreferencesDataSource.setEmergencySliderRange(range = range)
    }

    override val emergencySliderRange: Flow<IntRange>
        get() = qrAlarmPreferencesDataSource.getEmergencySliderRange().map {
            it ?: EMERGENCY_DEFAULT_SLIDER_RANGE
        }

    override suspend fun setEmergencyRequiredMatches(matches: Int) {
        qrAlarmPreferencesDataSource.setEmergencyRequiredMatches(matches = matches)
    }

    override val emergencyRequiredMatches: Flow<Int>
        get() = qrAlarmPreferencesDataSource.getEmergencyRequiredMatches().map {
            it ?: EMERGENCY_DEFAULT_REQUIRED_MATCHES
        }
}
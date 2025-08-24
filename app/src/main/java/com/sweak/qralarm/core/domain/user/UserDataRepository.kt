package com.sweak.qralarm.core.domain.user

import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    suspend fun setIntroductionFinished(finished: Boolean)
    val isIntroductionFinished: Flow<Boolean>

    suspend fun setOptimizationGuideState(state: OptimizationGuideState)
    val optimizationGuideState: Flow<OptimizationGuideState>

    suspend fun setTemporaryScannedCode(code: String?)
    val temporaryScannedCode: Flow<String?>

    suspend fun setAlarmMissedDetected(detected: Boolean)
    val isAlarmMissedDetected: Flow<Boolean>

    suspend fun setNextRatePromptTimeInMillis(promptTime: Long)
    val nextRatePromptTimeInMillis: Flow<Long?>

    suspend fun setDefaultAlarmCode(code: String?)
    val defaultAlarmCode: Flow<String?>

    suspend fun setEmergencySliderRange(range: IntRange)
    val emergencySliderRange: Flow<IntRange>

    suspend fun setEmergencyRequiredMatches(matches: Int)
    val emergencyRequiredMatches: Flow<Int>

    enum class OptimizationGuideState {
        NONE, SHOULD_BE_SEEN, HAS_BEEN_SEEN
    }
}
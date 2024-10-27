package com.sweak.qralarm.core.domain.user

import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    suspend fun setIntroductionFinished(finished: Boolean)
    val isIntroductionFinished: Flow<Boolean>

    suspend fun setTemporaryScannedCode(code: String?)
    val temporaryScannedCode: Flow<String?>
}
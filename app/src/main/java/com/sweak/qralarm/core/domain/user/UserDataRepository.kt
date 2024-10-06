package com.sweak.qralarm.core.domain.user

import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    suspend fun setTemporaryScannedCode(code: String)
    val temporaryScannedCode: Flow<String?>
}
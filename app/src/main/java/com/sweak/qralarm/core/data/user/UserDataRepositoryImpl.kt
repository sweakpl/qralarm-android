package com.sweak.qralarm.core.data.user

import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.storage.datastore.QRAlarmPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val qrAlarmPreferencesDataSource: QRAlarmPreferencesDataSource
): UserDataRepository {

    override suspend fun setIntroductionFinished(finished: Boolean) {
        qrAlarmPreferencesDataSource.setIntroductionFinished(finished = finished)
    }

    override val isIntroductionFinished: Flow<Boolean>
        get() = qrAlarmPreferencesDataSource.getIntroductionFinished()

    override suspend fun setTemporaryScannedCode(code: String?) {
        qrAlarmPreferencesDataSource.setTemporaryScannedCode(code = code)
    }

    override val temporaryScannedCode: Flow<String?>
        get() = qrAlarmPreferencesDataSource.getTemporaryScannedCode()
}
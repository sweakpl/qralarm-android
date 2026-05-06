package com.sweak.qralarm.core.domain.user

import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ShouldShowWhatsNew @Inject constructor(
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(): Boolean {
        val isIntroductionFinished = userDataRepository.isIntroductionFinished.first()
        val lastShownVersionCode = userDataRepository.whatsNewLastShownVersionCode.first()
        return isIntroductionFinished && lastShownVersionCode != WHATS_NEW_VERSION_CODE
    }
}

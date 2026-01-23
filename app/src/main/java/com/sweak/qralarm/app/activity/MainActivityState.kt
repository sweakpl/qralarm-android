package com.sweak.qralarm.app.activity

import com.sweak.qralarm.core.domain.user.model.Theme

data class MainActivityState(
    val shouldShowSplashScreen: Boolean = true,
    val isIntroductionFinished: Boolean? = null,
    val rateQRAlarmPromptTimeInMillis: Long? = null,
    val theme: Theme = Theme.Default
)

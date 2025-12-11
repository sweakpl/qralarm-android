package com.sweak.qralarm.app.activity

data class MainActivityState(
    val shouldShowSplashScreen: Boolean = true,
    val isIntroductionFinished: Boolean? = null,
    val rateQRAlarmPromptTimeInMillis: Long? = null
)

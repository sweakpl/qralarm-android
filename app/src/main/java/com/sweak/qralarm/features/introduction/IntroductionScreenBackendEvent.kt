package com.sweak.qralarm.features.introduction

sealed class IntroductionScreenBackendEvent {
    data object IntroductionFinishConfirmed : IntroductionScreenBackendEvent()
}
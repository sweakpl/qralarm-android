package com.sweak.qralarm.features.introduction

sealed class IntroductionScreenUserEvent {
    data object ContinueClicked : IntroductionScreenUserEvent()
}
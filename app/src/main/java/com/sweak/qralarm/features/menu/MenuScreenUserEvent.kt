package com.sweak.qralarm.features.menu

sealed class MenuScreenUserEvent {
    data object OnBackClicked : MenuScreenUserEvent()
    data object OnIntroductionClicked : MenuScreenUserEvent()
}
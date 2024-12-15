package com.sweak.qralarm.features.rate

sealed class RateScreenUserEvent {
    data object RateMeClicked : RateScreenUserEvent()
    data object SomethingWrongClicked : RateScreenUserEvent()
    data class IsNeverShowAgainCheckedChanged(val checked: Boolean) : RateScreenUserEvent()
    data object NotNowClicked : RateScreenUserEvent()
}
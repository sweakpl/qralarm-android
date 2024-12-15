package com.sweak.qralarm.features.rate

sealed class RateScreenBackendEvent {
    data object RateMeClickProcessed : RateScreenBackendEvent()
    data object SomethingWrongClickProcessed : RateScreenBackendEvent()
    data object NotNowClickProcessed : RateScreenBackendEvent()
}
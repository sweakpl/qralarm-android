package com.sweak.qralarm.features.qralarm_pro.model

sealed interface QRAlarmProDistributionSource {
    data object GooglePlay : QRAlarmProDistributionSource
    data object ItchIo : QRAlarmProDistributionSource
}
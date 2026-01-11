package com.sweak.qralarm.features.qralarm_pro

import com.sweak.qralarm.features.qralarm_pro.model.QRAlarmProDistributionSource

sealed class QRAlarmProScreenUserEvent {
    data class GetQRAlarmProClicked(
        val qrAlarmProDistributionSource: QRAlarmProDistributionSource
    ) : QRAlarmProScreenUserEvent()
    data object NotNowClicked : QRAlarmProScreenUserEvent()
}
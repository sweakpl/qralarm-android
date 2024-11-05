package com.sweak.qralarm.features.qralarm_pro

sealed class QRAlarmProScreenUserEvent {
    data object GetQRAlarmProClicked : QRAlarmProScreenUserEvent()
    data object NotNowClicked : QRAlarmProScreenUserEvent()
}
package com.sweak.qralarm.features.qralarm_pro.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons

data class ChipFeature(
    @param:StringRes val titleResourceId: Int,
    val icon: @Composable () -> ImageVector
)

val qrAlarmProChipFeatures = listOf(
    ChipFeature(
        titleResourceId = R.string.alarms_chain,
        icon = { QRAlarmIcons.Chain }
    ),
    ChipFeature(
        titleResourceId = R.string.do_not_leave_alarm,
        icon = { QRAlarmIcons.DoNotLeaveAlarm }
    ),
    ChipFeature(
        titleResourceId = R.string.power_off_guard,
        icon = { QRAlarmIcons.PowerOffGuard }
    ),
    ChipFeature(
        titleResourceId = R.string.block_volume_down,
        icon = { QRAlarmIcons.Sound }
    ),
    ChipFeature(
        titleResourceId = R.string.keep_ringer_on,
        icon = { QRAlarmIcons.Sound }
    )
)
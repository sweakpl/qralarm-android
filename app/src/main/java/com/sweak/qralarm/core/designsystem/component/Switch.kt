package com.sweak.qralarm.core.designsystem.component

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sweak.qralarm.core.designsystem.theme.LocalQRAlarmSwitchColors

@Composable
fun QRAlarmSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = LocalQRAlarmSwitchColors.current,
        modifier = modifier
    )
}
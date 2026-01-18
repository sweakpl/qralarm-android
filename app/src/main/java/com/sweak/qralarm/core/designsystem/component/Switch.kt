package com.sweak.qralarm.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Mischka
import com.sweak.qralarm.core.designsystem.theme.Mobster
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme

@Composable
fun QRAlarmSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = if (MaterialTheme.isQRAlarmTheme) {
            SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedBorderColor = BlueZodiac,
                checkedTrackColor = BlueZodiac,
                uncheckedThumbColor = Mobster,
                uncheckedBorderColor = Mobster,
                uncheckedTrackColor = Mischka
            )
        } else SwitchDefaults.colors(),
        modifier = modifier
    )
}
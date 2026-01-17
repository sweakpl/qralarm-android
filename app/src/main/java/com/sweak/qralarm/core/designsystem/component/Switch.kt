package com.sweak.qralarm.core.designsystem.component

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Deprecated("Use Material3 Switch directly instead", replaceWith = ReplaceWith("Switch"))
@Composable
fun QRAlarmSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
    )
}
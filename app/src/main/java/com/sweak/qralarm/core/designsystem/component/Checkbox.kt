package com.sweak.qralarm.core.designsystem.component

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme

@Composable
fun QRAlarmCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = if (MaterialTheme.isQRAlarmTheme) {
            CheckboxDefaults.colors(
                checkedColor = BlueZodiac,
                uncheckedColor = Color.White
            )
        } else CheckboxDefaults.colors(),
        modifier = modifier
    )
}

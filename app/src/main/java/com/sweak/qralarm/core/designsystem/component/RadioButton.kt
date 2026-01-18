package com.sweak.qralarm.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme

@Composable
fun QRAlarmRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        colors = if (MaterialTheme.isQRAlarmTheme) {
            RadioButtonDefaults.colors(
                selectedColor = BlueZodiac,
                unselectedColor = Color.White
            )
        } else RadioButtonDefaults.colors(),
        modifier = modifier
    )
}
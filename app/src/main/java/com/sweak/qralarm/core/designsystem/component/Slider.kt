package com.sweak.qralarm.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme

@Composable
fun QRAlarmSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int= 0,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Slider(
        enabled = enabled,
        value = value,
        valueRange = valueRange,
        steps = steps,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        colors = SliderDefaults.colors(
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
            thumbColor =
                if (MaterialTheme.isQRAlarmTheme) BlueZodiac else Color.Unspecified,
            activeTrackColor =
                if (MaterialTheme.isQRAlarmTheme) BlueZodiac else Color.Unspecified,
            inactiveTrackColor =
                if (MaterialTheme.isQRAlarmTheme) Color.White else Color.Unspecified,
            disabledActiveTickColor = Color.Transparent,
            disabledInactiveTickColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Preview
@Composable
private fun QRAlarmSliderPreview() {
    QRAlarmTheme {
        QRAlarmSlider(
            enabled = true,
            value = 5f,
            valueRange = 0f..10f,
            steps = 10,
            onValueChange = {},
            onValueChangeFinished = {}
        )
    }
}
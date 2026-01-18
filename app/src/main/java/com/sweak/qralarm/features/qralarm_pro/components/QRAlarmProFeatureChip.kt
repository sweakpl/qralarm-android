package com.sweak.qralarm.features.qralarm_pro.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.ButterflyBush
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.features.qralarm_pro.model.ChipFeature

@Composable
fun QRAlarmProFeatureChip(
    chipFeature: ChipFeature,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = { /* no-op */ },
        label = {
            Text(text = stringResource(chipFeature.titleResourceId))
        },
        icon = {
            Icon(
                imageVector = chipFeature.icon(),
                contentDescription = null
            )
        },
        enabled = false, // Disabled to prevent ripple effect on click
        colors = SuggestionChipDefaults.suggestionChipColors(
            disabledContainerColor = ButterflyBush,
            disabledLabelColor = Color.White,
            disabledIconContentColor = Color.White
        ),
        border = BorderStroke(0.dp, ButterflyBush),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun QRAlarmProFeatureChipPreview() {
    QRAlarmTheme {
        QRAlarmProFeatureChip(
            chipFeature = ChipFeature(
                titleResourceId = R.string.do_not_leave_alarm,
                icon = { QRAlarmIcons.DoNotLeaveAlarm }
            )
        )
    }
}
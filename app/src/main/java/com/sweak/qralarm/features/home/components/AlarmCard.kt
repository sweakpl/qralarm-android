package com.sweak.qralarm.features.home.components

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmCard
import com.sweak.qralarm.core.designsystem.component.QRAlarmSwitch
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.getAlarmRepeatingScheduleString
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.util.getTimeString
import com.sweak.qralarm.features.home.components.model.AlarmWrapper

@Composable
fun AlarmCard(
    alarmWrapper: AlarmWrapper,
    onClick: (AlarmWrapper) -> Unit,
    onAlarmEnabledChanged: (AlarmWrapper, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    QRAlarmCard(
        modifier = modifier
            .clickable { onClick(alarmWrapper) }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.space.medium,
                    vertical = MaterialTheme.space.smallMedium
                )
        ) {
            Column {
                Text(
                    text = getTimeString(
                        alarmWrapper.alarmHourOfDay,
                        alarmWrapper.alarmMinute,
                        DateFormat.is24HourFormat(LocalContext.current)
                    ),
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    text = getAlarmRepeatingScheduleString(
                        alarmWrapper.alarmRepeatingScheduleWrapper
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!alarmWrapper.isQRCOdeEnabled) {
                    Icon(
                        imageVector = QRAlarmIcons.NoQRCode,
                        contentDescription =
                        stringResource(R.string.content_description_no_qr_code_icon),
                        modifier = Modifier.size(size = MaterialTheme.space.large)
                    )

                    Spacer(modifier = Modifier.width(width = MaterialTheme.space.medium))
                }

                QRAlarmSwitch(
                    checked = alarmWrapper.isAlarmEnabled,
                    onCheckedChange = {
                        onAlarmEnabledChanged(alarmWrapper, it)
                    }
                )
            }
        }
    }
}



@Preview
@Composable
private fun AlarmCardPreview() {
    QRAlarmTheme {
        var alarmWrapper by remember {
            mutableStateOf(
                AlarmWrapper(
                    alarmHourOfDay = 8,
                    alarmMinute = 0,
                    alarmRepeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(),
                    isAlarmEnabled = true,
                    isQRCOdeEnabled = false
                )
            )
        }

        AlarmCard(
            alarmWrapper = alarmWrapper,
            onClick = {},
            onAlarmEnabledChanged = { _, enabled ->
                alarmWrapper = alarmWrapper.copy(isAlarmEnabled = enabled)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
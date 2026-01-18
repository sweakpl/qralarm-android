package com.sweak.qralarm.features.add_edit_alarm.destinations.alarms_chain.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.getTimeString

@Composable
fun OriginalAlarmCard(
    alarmHourOfDay: Int,
    alarmMinute: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = MaterialTheme.space.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = MaterialTheme.space.mediumLarge)
            ) {
                Icon(
                    imageVector = QRAlarmIcons.QRAlarm,
                    contentDescription = stringResource(R.string.content_description_qralarm_icon)
                )

                Text(
                    text = stringResource(R.string.this_alarm),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = MaterialTheme.space.smallMedium)
                )
            }

            Text(
                text = getTimeString(
                    hourOfDay = alarmHourOfDay,
                    minute = alarmMinute,
                    is24HourFormat = DateFormat.is24HourFormat(LocalContext.current)
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun OriginalAlarmCardPreview() {
    QRAlarmTheme {
        OriginalAlarmCard(
            alarmHourOfDay = 9,
            alarmMinute = 30,
            modifier = Modifier.width(400.dp)
        )
    }
}
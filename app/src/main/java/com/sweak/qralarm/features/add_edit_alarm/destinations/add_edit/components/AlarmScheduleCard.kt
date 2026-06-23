package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.getAlarmInString
import com.sweak.qralarm.core.ui.getDayString

@Composable
fun AlarmScheduleCard(
    daysHoursAndMinutesUntilAlarm: Triple<Int, Int, Int>,
    onlyOnceAlarmDateInMillis: Long,
    isOnlyOnce: Boolean,
    onAlarmDateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = MaterialTheme.space.medium)
        ) {
            Icon(
                imageVector = QRAlarmIcons.Alarm,
                contentDescription = stringResource(
                    R.string.content_description_alarm_icon
                ),
                modifier = Modifier
                    .padding(end = MaterialTheme.space.medium)
                    .size(size = MaterialTheme.space.mediumLarge)
            )

            Text(
                text = getAlarmInString(
                    daysHoursAndMinutesUntilAlarm = daysHoursAndMinutesUntilAlarm
                ),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AnimatedVisibility(visible = isOnlyOnce) {
            Column {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = LocalContentColor.current,
                    modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                )

                Box(
                    modifier = Modifier.clickable { onAlarmDateClicked() }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = MaterialTheme.space.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = MaterialTheme.space.smallMedium)
                        ) {
                            Icon(
                                imageVector = QRAlarmIcons.CalendarMonth,
                                contentDescription = stringResource(
                                    R.string.content_description_calendar_icon
                                ),
                                modifier = Modifier
                                    .padding(end = MaterialTheme.space.medium)
                                    .size(size = MaterialTheme.space.mediumLarge)
                            )

                            Text(
                                text = stringResource(R.string.alarm_date),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = getDayString(onlyOnceAlarmDateInMillis),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(end = MaterialTheme.space.small)
                            )

                            Icon(
                                imageVector = QRAlarmIcons.ForwardArrow,
                                contentDescription = stringResource(
                                    R.string.content_description_forward_arrow_icon
                                ),
                                modifier = Modifier.size(size = MaterialTheme.space.medium)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlarmScheduleCardOnlyOncePreview() {
    QRAlarmTheme {
        AlarmScheduleCard(
            daysHoursAndMinutesUntilAlarm = Triple(0, 7, 43),
            onlyOnceAlarmDateInMillis = System.currentTimeMillis(),
            isOnlyOnce = true,
            onAlarmDateClicked = {}
        )
    }
}

@Preview
@Composable
private fun AlarmScheduleCardRepeatingPreview() {
    QRAlarmTheme {
        AlarmScheduleCard(
            daysHoursAndMinutesUntilAlarm = Triple(1, 3, 12),
            onlyOnceAlarmDateInMillis = System.currentTimeMillis(),
            isOnlyOnce = false,
            onAlarmDateClicked = {}
        )
    }
}

package com.sweak.qralarm.features.home.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmCard
import com.sweak.qralarm.core.designsystem.component.QRAlarmSwitch
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.getAlarmRepeatingScheduleString
import com.sweak.qralarm.core.ui.getDayString
import com.sweak.qralarm.core.ui.getTimeString
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.EVERYDAY
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.features.home.components.model.AlarmWrapper

@Composable
fun AlarmCard(
    alarmWrapper: AlarmWrapper,
    onClick: (alarmId: Long) -> Unit,
    onAlarmEnabledChanged: (alarmId: Long, enabled: Boolean) -> Unit,
    onDeleteAlarmClick: (alarmId: Long) -> Unit,
    onSkipNextAlarmChanged: (alarmId: Long, skip: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    QRAlarmCard(modifier = modifier.clickable { onClick(alarmWrapper.alarmId) }) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.space.medium,
                    top = MaterialTheme.space.smallMedium,
                    bottom = MaterialTheme.space.smallMedium
                )
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AnimatedVisibility(visible = alarmWrapper.alarmLabel != null) {
                    Text(
                        text = alarmWrapper.alarmLabel ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val indicateSkippingNextAlarm =
                    alarmWrapper.skipNextAlarmConfig.isSkippingSupported &&
                            alarmWrapper.skipNextAlarmConfig.isSkippingNextAlarm

                Text(
                    text = getTimeString(
                        hourOfDay = alarmWrapper.alarmHourOfDay,
                        minute = alarmWrapper.alarmMinute,
                        is24HourFormat = DateFormat.is24HourFormat(LocalContext.current)
                    ),
                    style = MaterialTheme.typography.displayLarge.copy(
                        textDecoration =
                        if (indicateSkippingNextAlarm) TextDecoration.LineThrough
                        else TextDecoration.None
                    ),
                    color =
                    if (indicateSkippingNextAlarm) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = getAlarmRepeatingScheduleString(
                        alarmWrapper.alarmRepeatingScheduleWrapper
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )

                AnimatedVisibility(
                    visible = alarmWrapper.isAlarmEnabled &&
                            alarmWrapper.alarmRepeatingScheduleWrapper.alarmRepeatingMode != ONLY_ONCE
                ) {
                    Text(
                        text = stringResource(
                            R.string.next_alarm_date,
                            getDayString(alarmWrapper.nextAlarmTimeInMillis)
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!alarmWrapper.isCodeEnabled) {
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
                        onAlarmEnabledChanged(alarmWrapper.alarmId, it)
                    }
                )

                var expanded by remember { mutableStateOf(false) }

                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = QRAlarmIcons.More,
                        contentDescription = stringResource(R.string.content_description_more_icon)
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .wrapContentWidth()
                            .background(color = MaterialTheme.colorScheme.surface)
                    ) {
                        if (alarmWrapper.skipNextAlarmConfig.isSkippingSupported) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(
                                            if (alarmWrapper.skipNextAlarmConfig.isSkippingNextAlarm) {
                                                R.string.undo_skipping_next_alarm
                                            } else {
                                                R.string.skip_next_alarm
                                            }
                                        ),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                        if (alarmWrapper.skipNextAlarmConfig.isSkippingNextAlarm) {
                                            QRAlarmIcons.Undo
                                        } else {
                                            QRAlarmIcons.SkipNextAlarm
                                        },
                                        contentDescription = stringResource(
                                            if (alarmWrapper.skipNextAlarmConfig.isSkippingNextAlarm) {
                                                R.string.content_description_no_qr_code_icon
                                            } else {
                                                R.string.content_description_skip_next_alarm_icon
                                            }
                                        ),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    onSkipNextAlarmChanged(
                                        alarmWrapper.alarmId,
                                        !alarmWrapper.skipNextAlarmConfig.isSkippingNextAlarm
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.delete_alarm),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = QRAlarmIcons.Delete,
                                    contentDescription = stringResource(
                                        R.string.content_description_delete_icon
                                    ),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                expanded = false
                                onDeleteAlarmClick(alarmWrapper.alarmId)
                            }
                        )
                    }
                }
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
                    alarmId = 0,
                    alarmHourOfDay = 8,
                    alarmMinute = 0,
                    alarmLabel = "Work alarm",
                    nextAlarmTimeInMillis = 1732604400000,
                    alarmRepeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(
                        alarmRepeatingMode = EVERYDAY
                    ),
                    isAlarmEnabled = true,
                    isCodeEnabled = false,
                    skipNextAlarmConfig = AlarmWrapper.SkipNextAlarmConfig(
                        isSkippingSupported = true,
                        isSkippingNextAlarm = false
                    )
                )
            )
        }

        AlarmCard(
            alarmWrapper = alarmWrapper,
            onClick = {},
            onAlarmEnabledChanged = { _, enabled ->
                alarmWrapper = alarmWrapper.copy(isAlarmEnabled = enabled)
            },
            onDeleteAlarmClick = {},
            onSkipNextAlarmChanged = { _, _ -> },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
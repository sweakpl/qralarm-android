package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.alarm.AlarmRingtone
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmRingtoneWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAlarmRingtoneDialogBottomSheet(
    initialAlarmRingtoneWrapper: AlarmRingtoneWrapper,
    availableAlarmRingtones: List<AlarmRingtone>,
    onDismissRequest: (newAlarmRingtoneWrapper: AlarmRingtoneWrapper) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedAlarmRingtone = remember {
        if (initialAlarmRingtoneWrapper is AlarmRingtoneWrapper.OriginalRingtone) {
            mutableStateOf(initialAlarmRingtoneWrapper.alarmRingtone)
        } else {
            mutableStateOf(AlarmRingtone.CUSTOM_SOUND)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            when (selectedAlarmRingtone.value) {
                AlarmRingtone.GENTLE_GUITAR -> {
                    onDismissRequest(AlarmRingtoneWrapper.OriginalRingtone.GentleGuitar)
                }
                AlarmRingtone.ALARM_CLOCK -> {
                    onDismissRequest(AlarmRingtoneWrapper.OriginalRingtone.AlarmClock)
                }
                AlarmRingtone.AIR_HORN -> {
                    onDismissRequest(AlarmRingtoneWrapper.OriginalRingtone.AirHorn)
                }
                AlarmRingtone.CUSTOM_SOUND -> {
                    onDismissRequest(
                        AlarmRingtoneWrapper.CustomRingtone(customSoundUriString = null)
                    )
                }
            }
        },
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.tertiary
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.space.mediumLarge,
                    end = MaterialTheme.space.mediumLarge,
                    bottom = MaterialTheme.space.xLarge
                )
        ) {
            Text(
                text = stringResource(R.string.ringtone),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
            )

            Column(modifier = Modifier.selectableGroup()) {
                availableAlarmRingtones.forEach { alarmRingtone ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedAlarmRingtone.value == alarmRingtone,
                                onClick = { selectedAlarmRingtone.value = alarmRingtone },
                                role = Role.RadioButton
                            )
                    ) {
                        Row {
                            RadioButton(
                                selected = selectedAlarmRingtone.value == alarmRingtone,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.secondary,
                                    unselectedColor = MaterialTheme.colorScheme.onTertiary
                                )
                            )

                            Text(
                                text = stringResource(
                                    when (alarmRingtone) {
                                        AlarmRingtone.GENTLE_GUITAR -> R.string.gentle_guitar
                                        AlarmRingtone.ALARM_CLOCK -> R.string.alarm_clock
                                        AlarmRingtone.AIR_HORN -> R.string.air_horn
                                        AlarmRingtone.CUSTOM_SOUND -> R.string.custom_sound
                                    }
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = MaterialTheme.space.medium)
                            )
                        }

                        IconButton(
                            onClick = { /* TODO */ }
                        ) {
                            Icon(
                                imageVector = QRAlarmIcons.Play,
                                contentDescription =
                                stringResource(R.string.content_description_play_icon)
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
private fun ChooseAlarmRingtoneDialogBottomSheetPreview() {
    QRAlarmTheme {
        ChooseAlarmRingtoneDialogBottomSheet(
            initialAlarmRingtoneWrapper = AlarmRingtoneWrapper.OriginalRingtone.GentleGuitar,
            availableAlarmRingtones = AlarmRingtone.entries,
            onDismissRequest = {}
        )
    }
}
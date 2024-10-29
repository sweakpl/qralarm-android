package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAlarmRingtoneDialogBottomSheet(
    initialRingtone: Ringtone,
    availableRingtonesWithPlaybackState: Map<Ringtone, Boolean>,
    isCustomRingtoneUploaded: Boolean,
    onTogglePlaybackState: (Ringtone) -> Unit,
    onPickCustomRingtone: () -> Unit,
    onDismissRequest: (newRingtone: Ringtone) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedAlarmRingtone by remember(initialRingtone) {
        mutableStateOf(initialRingtone)
    }

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest(selectedAlarmRingtone) },
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface
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
                availableRingtonesWithPlaybackState.forEach { (alarmRingtone, playbackState) ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = MaterialTheme.space.xLarge)
                            .selectable(
                                selected = selectedAlarmRingtone == alarmRingtone,
                                onClick = {
                                    if (alarmRingtone == Ringtone.CUSTOM_SOUND &&
                                        !isCustomRingtoneUploaded
                                    ) {
                                        onPickCustomRingtone()
                                    } else {
                                        selectedAlarmRingtone = alarmRingtone
                                    }
                                },
                                role = Role.RadioButton
                            )
                    ) {
                        Row {
                            RadioButton(
                                selected = selectedAlarmRingtone == alarmRingtone,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.secondary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Text(
                                text = stringResource(
                                    when (alarmRingtone) {
                                        Ringtone.GENTLE_GUITAR -> R.string.gentle_guitar
                                        Ringtone.KALIMBA -> R.string.kalimba
                                        Ringtone.CLASSIC_ALARM -> R.string.classic_alarm
                                        Ringtone.ALARM_CLOCK -> R.string.alarm_clock
                                        Ringtone.ROOSTER -> R.string.rooster
                                        Ringtone.AIR_HORN -> R.string.air_horn
                                        Ringtone.CUSTOM_SOUND -> R.string.custom_sound
                                    }
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = MaterialTheme.space.medium)
                            )
                        }

                        Row {
                            if (alarmRingtone == Ringtone.CUSTOM_SOUND &&
                                isCustomRingtoneUploaded
                            ) {
                                IconButton(onClick = { onPickCustomRingtone() }) {
                                    Icon(
                                        imageVector = QRAlarmIcons.Edit,
                                        contentDescription =
                                        stringResource(R.string.content_description_edit_icon)
                                    )
                                }
                            }

                            if (alarmRingtone != Ringtone.CUSTOM_SOUND ||
                                isCustomRingtoneUploaded
                            ) {
                                IconButton(onClick = { onTogglePlaybackState(alarmRingtone) }) {
                                    Icon(
                                        imageVector =
                                        if (playbackState) QRAlarmIcons.Stop else QRAlarmIcons.Play,
                                        contentDescription = stringResource(
                                            if (playbackState) R.string.content_description_stop_icon
                                            else R.string.content_description_play_icon
                                        )
                                    )
                                }
                            }
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
            initialRingtone= Ringtone.GENTLE_GUITAR,
            availableRingtonesWithPlaybackState = Ringtone.entries.associateWith { false },
            isCustomRingtoneUploaded = true,
            onTogglePlaybackState = {},
            onPickCustomRingtone = {},
            onDismissRequest = {}
        )
    }
}
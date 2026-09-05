package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmCheckbox
import com.sweak.qralarm.core.designsystem.component.QRAlarmRadioButton
import com.sweak.qralarm.core.designsystem.component.QRAlarmSlider
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAlarmRingtoneConfigDialogBottomSheet(
    initialRingtone: Ringtone,
    availableRingtonesWithPlaybackState: Map<Ringtone, Boolean>,
    isCustomRingtoneUploaded: Boolean,
    onTogglePlaybackState: (Ringtone) -> Unit,
    onPickCustomRingtone: () -> Unit,
    alarmVolumePercentage: Int?,
    onDismissRequest: (newRingtone: Ringtone, newAlarmVolumePercentage: Int?) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedAlarmRingtone by remember(initialRingtone) { mutableStateOf(initialRingtone) }
    var isUsingSystemVolume by remember(alarmVolumePercentage) {
        mutableStateOf(alarmVolumePercentage == null)
    }
    var selectedAlarmVolumePercentage by remember(alarmVolumePercentage) {
        mutableIntStateOf(alarmVolumePercentage ?: 50)
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest(
                selectedAlarmRingtone,
                if (isUsingSystemVolume) null else selectedAlarmVolumePercentage
            )
        },
        sheetState = modalBottomSheetState
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
            ) {
                Icon(
                    imageVector = when (selectedAlarmVolumePercentage) {
                        in 0..33 -> QRAlarmIcons.SoundLow
                        in 34..66 -> QRAlarmIcons.SoundMedium
                        else -> QRAlarmIcons.Sound
                    },
                    contentDescription = stringResource(R.string.content_description_sound_icon),
                    modifier = Modifier.size(size = MaterialTheme.space.large)
                )

                QRAlarmSlider(
                    value = selectedAlarmVolumePercentage.toFloat(),
                    enabled = !isUsingSystemVolume,
                    valueRange = 0f..100f,
                    steps = 9,
                    onValueChange = { newValue ->
                        selectedAlarmVolumePercentage =
                            if (newValue < 10f) 10 else newValue.toInt()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = MaterialTheme.space.medium)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .toggleable(
                        value = isUsingSystemVolume,
                        onValueChange = { isUsingSystemVolume = it },
                        role = Role.Checkbox
                    )
            ) {
                QRAlarmCheckbox(
                    checked = isUsingSystemVolume,
                    onCheckedChange = null
                )

                Text(
                    text = stringResource(R.string.using_system_alarm_volume),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = MaterialTheme.space.medium)
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = LocalContentColor.current,
                modifier = Modifier.padding(vertical = MaterialTheme.space.mediumLarge)
            )

            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(state = rememberScrollState())
            ) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(weight = 1f)
                        ) {
                            QRAlarmRadioButton(
                                selected = selectedAlarmRingtone == alarmRingtone,
                                onClick = null
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
                                        Ringtone.SYSTEM_DEFAULT -> R.string.system_default_ringtone
                                        Ringtone.CUSTOM_SOUND -> R.string.custom_sound
                                    }
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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
        ChooseAlarmRingtoneConfigDialogBottomSheet(
            initialRingtone = Ringtone.GENTLE_GUITAR,
            availableRingtonesWithPlaybackState = Ringtone.entries.associateWith { false },
            isCustomRingtoneUploaded = true,
            onTogglePlaybackState = {},
            onPickCustomRingtone = {},
            alarmVolumePercentage = null,
            onDismissRequest = { _, _ -> }
        )
    }
}
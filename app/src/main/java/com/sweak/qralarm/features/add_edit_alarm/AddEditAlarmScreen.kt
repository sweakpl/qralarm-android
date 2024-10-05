package com.sweak.qralarm.features.add_edit_alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmCard
import com.sweak.qralarm.core.designsystem.component.QRAlarmSwitch
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.alarm.AlarmRingtone
import com.sweak.qralarm.core.ui.util.shortName
import com.sweak.qralarm.features.add_edit_alarm.components.ChooseAlarmRepeatingScheduleBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.ChooseAlarmRingtoneDialogBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.ChooseGentleWakeUpDurationBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.ChooseSnoozeConfigurationBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.QRAlarmTimePicker
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmSnoozeConfigurationWrapper
import java.time.DayOfWeek

@Composable
fun AddEditAlarmScreen(onCancelClicked: () -> Unit) {
    val addEditAlarmViewModel = hiltViewModel<AddEditAlarmViewModel>()
    val addEditAlarmScreenState by addEditAlarmViewModel.state.collectAsStateWithLifecycle()

    AddEditAlarmScreenContent(
        state = addEditAlarmScreenState,
        onEvent = { event ->
            when (event) {
                is AddEditAlarmScreenUserEvent.OnCancelClicked -> onCancelClicked()
                else -> addEditAlarmViewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditAlarmScreenContent(
    state: AddEditAlarmScreenState,
    onEvent: (AddEditAlarmScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.alarm),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(AddEditAlarmScreenUserEvent.OnCancelClicked) }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.Close,
                            contentDescription =
                            stringResource(R.string.content_description_close_icon)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO */ }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.Done,
                            contentDescription =
                            stringResource(R.string.content_description_done_icon)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxWidth()
            ) {
                if (state.alarmHourOfDay != null && state.alarmMinute != null) {
                    QRAlarmTimePicker(
                        selectedHourOfDay = state.alarmHourOfDay,
                        selectedMinute = state.alarmMinute,
                        onTimeChanged = { hourOfDay, minute ->
                            onEvent(
                                AddEditAlarmScreenUserEvent.AlarmTimeChanged(
                                    newAlarmHourOfDay = hourOfDay,
                                    newAlarmMinute = minute
                                )
                            )
                        },
                        is24HoursView = true,
                        isEnabled = true,
                        modifier = Modifier.padding(vertical = MaterialTheme.space.mediumLarge)
                    )
                }

                QRAlarmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.mediumLarge
                        )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.space.medium,
                                vertical = MaterialTheme.space.small
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.alarm_enabled),
                            style = MaterialTheme.typography.titleLarge
                        )

                        QRAlarmSwitch(
                            checked = state.isAlarmEnabled,
                            onCheckedChange = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent.AlarmEnabledChanged(isEnabled = it)
                                )
                            }
                        )
                    }
                }

                QRAlarmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.mediumLarge
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                onEvent(
                                    AddEditAlarmScreenUserEvent
                                        .ChooseAlarmRepeatingScheduleDialogVisible(isVisible = true)
                                )
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = MaterialTheme.space.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.repeating),
                                style = MaterialTheme.typography.titleLarge
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getAlarmRepeatingScheduleString(
                                        state.alarmRepeatingScheduleWrapper
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(end = MaterialTheme.space.small)
                                )

                                Icon(
                                    imageVector = QRAlarmIcons.ForwardArrow,
                                    contentDescription =
                                    stringResource(R.string.content_description_forward_arrow_icon),
                                    modifier = Modifier.size(size = MaterialTheme.space.medium)
                                )
                            }
                        }
                    }

                    Separator()

                    Box(
                        modifier = Modifier
                            .clickable {
                                onEvent(
                                    AddEditAlarmScreenUserEvent
                                        .ChooseAlarmSnoozeConfigurationDialogVisible(
                                            isVisible = true
                                        )
                                )
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = MaterialTheme.space.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.snooze),
                                style = MaterialTheme.typography.titleLarge
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getAlarmSnoozeConfigurationString(
                                        state.alarmSnoozeConfigurationWrapper
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(end = MaterialTheme.space.small)
                                )

                                Icon(
                                    imageVector = QRAlarmIcons.ForwardArrow,
                                    contentDescription =
                                    stringResource(R.string.content_description_forward_arrow_icon),
                                    modifier = Modifier.size(size = MaterialTheme.space.medium)
                                )
                            }
                        }
                    }

                    Separator()

                    Box(
                        modifier = Modifier
                            .clickable {
                                onEvent(
                                    AddEditAlarmScreenUserEvent.ChooseAlarmRingtoneDialogVisible(
                                        isVisible = true
                                    )
                                )
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = MaterialTheme.space.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.ringtone),
                                style = MaterialTheme.typography.titleLarge
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getAlarmRingtoneString(state.alarmRingtone),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(end = MaterialTheme.space.small)
                                )

                                Icon(
                                    imageVector = QRAlarmIcons.ForwardArrow,
                                    contentDescription =
                                    stringResource(R.string.content_description_forward_arrow_icon),
                                    modifier = Modifier.size(size = MaterialTheme.space.medium)
                                )
                            }
                        }
                    }

                    Separator()

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.space.medium,
                                vertical = MaterialTheme.space.small
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.vibrations),
                            style = MaterialTheme.typography.titleLarge
                        )

                        QRAlarmSwitch(
                            checked = state.areVibrationsEnabled,
                            onCheckedChange = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent.VibrationsEnabledChanged(
                                        areEnabled = it
                                    )
                                )
                            }
                        )
                    }
                }

                QRAlarmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.mediumLarge
                        )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.space.medium,
                                vertical = MaterialTheme.space.small
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.use_qr_bar_code),
                            style = MaterialTheme.typography.titleLarge
                        )

                        QRAlarmSwitch(
                            checked = state.isCodeEnabled,
                            onCheckedChange = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent.CodeEnabledChanged(isEnabled = it)
                                )
                            }
                        )
                    }

                    AnimatedVisibility(visible = state.isCodeEnabled) {
                        Column {
                            Separator()

                            Text(
                                text = stringResource(R.string.assign_specific_code),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(
                                        start = MaterialTheme.space.medium,
                                        top = MaterialTheme.space.medium,
                                        end = MaterialTheme.space.medium,
                                        bottom = MaterialTheme.space.xSmall
                                    )
                            )

                            Text(
                                text = stringResource(R.string.assign_specific_code_description),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                            )

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier
                                    .padding(all = MaterialTheme.space.medium)
                                    .clickable { /* TODO */ }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = MaterialTheme.space.medium)
                                ) {
                                    Text(
                                        text = stringResource(R.string.scan_your_own_code),
                                        style = MaterialTheme.typography.labelLarge
                                    )

                                    Icon(
                                        imageVector = QRAlarmIcons.ForwardArrow,
                                        contentDescription =
                                        stringResource(R.string.content_description_forward_arrow_icon)
                                    )
                                }
                            }
                        }
                    }
                }

                QRAlarmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.mediumLarge
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                onEvent(
                                    AddEditAlarmScreenUserEvent
                                        .ChooseGentleWakeUpDurationDialogVisible(isVisible = true)
                                )
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = MaterialTheme.space.medium)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = MaterialTheme.space.smallMedium)
                            ) {
                                Text(
                                    text = stringResource(R.string.gentle_wake_up),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                                )

                                Text(
                                    text = stringResource(R.string.gentle_wake_up_description),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getGentleWakeUpDurationString(
                                        state.gentleWakeupDurationInSeconds
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(end = MaterialTheme.space.small)
                                )

                                Icon(
                                    imageVector = QRAlarmIcons.ForwardArrow,
                                    contentDescription =
                                    stringResource(R.string.content_description_forward_arrow_icon),
                                    modifier = Modifier.size(size = MaterialTheme.space.medium)
                                )
                            }
                        }
                    }

                    Separator()

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = MaterialTheme.space.medium)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = MaterialTheme.space.smallMedium)
                        ) {
                            Text(
                                text = stringResource(R.string.temporary_mute),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                            )

                            Text(
                                text = stringResource(R.string.temporary_mute_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        QRAlarmSwitch(
                            checked = state.isTemporaryMuteEnabled,
                            onCheckedChange = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent.TemporaryMuteEnabledChanged(
                                        isEnabled = it
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (state.isChooseAlarmRepeatingScheduleDialogVisible) {
        ChooseAlarmRepeatingScheduleBottomSheet(
            initialAlarmRepeatingScheduleWrapper = state.alarmRepeatingScheduleWrapper,
            onDismissRequest = { newAlarmRepeatingSchedule ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmRepeatingScheduleSelected(
                        newAlarmRepeatingScheduleWrapper = newAlarmRepeatingSchedule
                    )
                )
            }
        )
    }

    if (state.isChooseAlarmSnoozeConfigurationDialogVisible) {
        ChooseSnoozeConfigurationBottomSheet(
            initialAlarmSnoozeConfigurationWrapper = state.alarmSnoozeConfigurationWrapper,
            availableSnoozeNumbers = state.availableSnoozeNumbers,
            availableSnoozeDurationsInMinutes = state.availableSnoozeDurationsInMinutes,
            onDismissRequest = { newAlarmSnoozeConfigurationWrapper ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmSnoozeConfigurationSelected(
                        newAlarmSnoozeConfigurationWrapper = newAlarmSnoozeConfigurationWrapper
                    )
                )
            }
        )
    }

    if (state.isChooseAlarmRingtoneDialogVisible) {
        ChooseAlarmRingtoneDialogBottomSheet(
            initialAlarmRingtone = state.alarmRingtone,
            availableAlarmRingtonesWithPlaybackState =
            state.availableAlarmRingtonesWithPlaybackState,
            onTogglePlaybackState = { toggledAlarmRingtone ->
                onEvent(
                    AddEditAlarmScreenUserEvent.ToggleAlarmRingtonePlayback(
                        alarmRingtone = toggledAlarmRingtone
                    )
                )
            },
            onDismissRequest = { newAlarmRingtone ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmRingtoneSelected(
                        newAlarmRingtone = newAlarmRingtone
                    )
                )
            }
        )
    }

    if (state.isChooseGentleWakeUpDurationDialogVisible) {
        ChooseGentleWakeUpDurationBottomSheet(
            initialGentleWakeUpDurationInSeconds = state.gentleWakeupDurationInSeconds,
            availableGentleWakeUpDurationsInSeconds = state.availableGentleWakeUpDurationsInSeconds,
            onDismissRequest = { newGentleWakeUpDurationInSeconds ->
                onEvent(
                    AddEditAlarmScreenUserEvent.GentleWakeUpDurationSelected(
                        newGentleWakeUpDurationInSeconds = newGentleWakeUpDurationInSeconds
                    )
                )
            }
        )
    }
}

@Composable
private fun Separator() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = MaterialTheme.space.medium)
            .background(color = MaterialTheme.colorScheme.onTertiary)
    )
}

@Composable
private fun getAlarmRepeatingScheduleString(
    alarmRepeatingScheduleWrapper: AlarmRepeatingScheduleWrapper
): String {
    return when (alarmRepeatingScheduleWrapper.alarmRepeatingMode) {
        AlarmRepeatingMode.ONLY_ONCE -> stringResource(R.string.only_once)
        AlarmRepeatingMode.MON_FRI -> {
            DayOfWeek.MONDAY.shortName() + " - " + DayOfWeek.FRIDAY.shortName()
        }

        AlarmRepeatingMode.SAT_SUN -> {
            DayOfWeek.SATURDAY.shortName() + ", " + DayOfWeek.SUNDAY.shortName()
        }

        AlarmRepeatingMode.CUSTOM -> {
            val days = alarmRepeatingScheduleWrapper.alarmDaysOfWeek

            if (days.size == 1) {
                return days.first().shortName()
            } else if (days.size == 2) {
                return days.joinToString { it.shortName() }
            } else if (areAllDaysAfterOneAnother(days)) {
                return days.first().shortName() + " - " + days.last().shortName()
            } else {
                return days.joinToString { it.shortName() }
            }
        }
    }
}

private fun areAllDaysAfterOneAnother(days: List<DayOfWeek>): Boolean {
    days.forEachIndexed { index, day ->
        if (index == days.size - 1) return true
        if (days[index + 1].value - day.value != 1) return false
    }

    return false
}

@Composable
private fun getAlarmSnoozeConfigurationString(
    alarmSnoozeConfigurationWrapper: AlarmSnoozeConfigurationWrapper
): String {
    if (alarmSnoozeConfigurationWrapper.numberOfSnoozes == 0) {
        return stringResource(R.string.no_snoozes)
    }

    return alarmSnoozeConfigurationWrapper.numberOfSnoozes.toString() +
            " x " +
            alarmSnoozeConfigurationWrapper.snoozeDurationInMinutes +
            " " + stringResource(R.string.min)
}

@Composable
fun getAlarmRingtoneString(alarmRingtone: AlarmRingtone): String {
    return when (alarmRingtone) {
        AlarmRingtone.GENTLE_GUITAR -> stringResource(R.string.gentle_guitar)
        AlarmRingtone.ALARM_CLOCK -> stringResource(R.string.alarm_clock)
        AlarmRingtone.AIR_HORN -> stringResource(R.string.air_horn)
        AlarmRingtone.CUSTOM_SOUND -> stringResource(R.string.custom_sound)
    }
}

@Composable
fun getGentleWakeUpDurationString(gentleWakeUpDurationInSeconds: Int): String {
    return if (gentleWakeUpDurationInSeconds == 0) {
        stringResource(R.string.disabled)
    } else {
        gentleWakeUpDurationInSeconds.toString() + " " + stringResource(R.string.sec)
    }
}

@Preview
@Composable
private fun AddEditAlarmScreenContentPreview() {
    QRAlarmTheme {
        AddEditAlarmScreenContent(
            state = AddEditAlarmScreenState(),
            onEvent = { }
        )
    }
}
package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmDialog
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.components.EditCodeNameBottomSheet
import com.sweak.qralarm.core.ui.components.MissingPermissionsBottomSheet
import com.sweak.qralarm.core.ui.components.NavigationButton
import com.sweak.qralarm.core.ui.components.ToggleSetting
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.core.ui.compose_util.getAlarmRepeatingScheduleString
import com.sweak.qralarm.core.ui.compose_util.rememberAlarmPermissionsRequester
import com.sweak.qralarm.core.ui.model.AlarmPermission
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
import com.sweak.qralarm.core.ui.toBidiIsolated
import com.sweak.qralarm.features.add_edit_alarm.components.ChoiceSetting
import com.sweak.qralarm.features.add_edit_alarm.components.SimpleSetting
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.AlarmDatePickerDialog
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.AlarmScheduleCard
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.AssignCodeBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.ChooseAlarmRepeatingScheduleBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.ChooseAlarmRingtoneConfigDialogBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.ChooseSnoozeConfigurationBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.DialerTimePickerDialog
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.DownloadCodeBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.components.QRAlarmTimePicker
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import com.sweak.qralarm.features.alarm.components.TimeTickReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun AddEditAlarmScreen(
    addEditAlarmViewModel: AddEditAlarmViewModel,
    onCancelClicked: () -> Unit,
    onAlarmSaved: () -> Unit,
    onScanCustomCodeClicked: () -> Unit,
    onAdvancedSettingsClicked: () -> Unit,
    onSpecialSettingsClicked: () -> Unit,
    onAlarmsChainSettingsClicked: () -> Unit,
    onAlarmDeleted: () -> Unit
) {
    val addEditAlarmScreenState by addEditAlarmViewModel.state.collectAsStateWithLifecycle()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.GetContent() {
            override fun createIntent(context: Context, input: String): Intent {
                return super.createIntent(context, input).apply {
                    putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                }
            }
        },
        onResult = { uri ->
            addEditAlarmViewModel.onEvent(
                AddEditAlarmScreenUserEvent.CustomRingtoneUriRetrieved(customRingtoneUri = uri)
            )
        }
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current

    val saveDefaultCodeImageLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.CreateDocument("image/png") {},
        onResult = { uri ->
            coroutineScope.launch(Dispatchers.IO) {
                if (uri == null) return@launch

                try {
                    val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "w")

                    parcelFileDescriptor?.use {
                        val qrCodeImageBitmap =
                            BitmapFactory.decodeResource(resources, R.drawable.qr_code)
                        val fileOutputStream = FileOutputStream(it.fileDescriptor)

                        if (
                            !qrCodeImageBitmap.compress(
                                Bitmap.CompressFormat.PNG,
                                95,
                                fileOutputStream
                            )
                        ) throw IOException()

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                resources.getString(R.string.saved_qr_code),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } ?: throw IOException()
                } catch (e: Exception) {
                    if (e is IOException || e is IllegalArgumentException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                resources.getString(R.string.could_not_save_qr_code),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        throw e
                    }
                }
            }
        }
    )

    var isInTheCameraPermissionFlowForCustomCodeScan by remember { mutableStateOf(false) }
    val permissionsRequester = rememberAlarmPermissionsRequester(
        onCameraRationaleRequired = {
            addEditAlarmViewModel.onEvent(
                AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible(isVisible = true)
            )
        },
        onNotificationsRationaleRequired = {
            addEditAlarmViewModel.onEvent(
                AddEditAlarmScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                    isVisible = true
                )
            )
        }
    )

    ObserveAsEvents(
        flow = addEditAlarmViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is AddEditAlarmScreenBackendEvent.AlarmChangesDiscarded -> onCancelClicked()
                is AddEditAlarmScreenBackendEvent.AlarmSaved -> onAlarmSaved()
                is AddEditAlarmScreenBackendEvent.CustomRingtoneRetrievalFinished -> {
                    Toast.makeText(
                        context,
                        if (event.isSuccess) R.string.ringtone_successfully_uploaded
                        else R.string.ringtone_upload_unsuccessful,
                        Toast.LENGTH_LONG
                    ).show()
                }

                is AddEditAlarmScreenBackendEvent.CustomCodeAssignmentFinished -> {
                    Toast.makeText(
                        context,
                        R.string.code_successfully_assigned,
                        Toast.LENGTH_LONG
                    ).show()
                }

                is AddEditAlarmScreenBackendEvent.AlarmDeleted -> onAlarmDeleted()
                is AddEditAlarmScreenBackendEvent.AlarmRingtonePreviewPlaybackError -> {
                    Toast.makeText(
                        context,
                        R.string.there_was_an_issue_playing_ringtone,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    OnResume {
        if (isInTheCameraPermissionFlowForCustomCodeScan) {
            isInTheCameraPermissionFlowForCustomCodeScan = false

            if (permissionsRequester.isCameraPermissionGranted) {
                onScanCustomCodeClicked()
            }
        } else if (addEditAlarmScreenState.ui.permissionsDialogState.isVisible) {
            addEditAlarmViewModel.onEvent(
                AddEditAlarmScreenUserEvent.TrySaveAlarm(
                    cameraPermissionStatus = permissionsRequester.isCameraPermissionGranted,
                    notificationsPermissionStatus =
                        permissionsRequester.isNotificationsPermissionGranted
                )
            )
        }
    }

    BackHandler {
        addEditAlarmViewModel.onEvent(AddEditAlarmScreenUserEvent.OnCancelClicked)
    }

    TimeTickReceiver {
        addEditAlarmViewModel.onEvent(AddEditAlarmScreenUserEvent.RefreshAlarmCountdown)
    }

    AddEditAlarmScreenContent(
        state = addEditAlarmScreenState,
        onEvent = { event ->
            when (event) {
                is AddEditAlarmScreenUserEvent.ConfirmDiscardAlarmChanges -> onCancelClicked()
                is AddEditAlarmScreenUserEvent.SaveAlarmClicked -> {
                    addEditAlarmViewModel.onEvent(
                        AddEditAlarmScreenUserEvent.TrySaveAlarm(
                            cameraPermissionStatus = permissionsRequester.isCameraPermissionGranted,
                            notificationsPermissionStatus =
                                permissionsRequester.isNotificationsPermissionGranted
                        )
                    )
                }

                is AddEditAlarmScreenUserEvent.RequestPermission -> {
                    permissionsRequester.request(event.permission)
                }

                is AddEditAlarmScreenUserEvent.PickCustomRingtone -> {
                    try {
                        audioPickerLauncher.launch("audio/*")
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.issue_opening_the_page),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is AddEditAlarmScreenUserEvent.TryScanSpecificCode -> {
                    addEditAlarmViewModel.onEvent(
                        AddEditAlarmScreenUserEvent.AssignCodeDialogVisible(isVisible = false)
                    )

                    if (!permissionsRequester.isCameraPermissionGranted) {
                        isInTheCameraPermissionFlowForCustomCodeScan = true
                        permissionsRequester.request(AlarmPermission.CAMERA)
                    } else {
                        onScanCustomCodeClicked()
                    }
                }

                is AddEditAlarmScreenUserEvent.GoToApplicationSettingsClicked -> {
                    addEditAlarmViewModel.onEvent(
                        AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible(
                            isVisible = false
                        )
                    )
                    addEditAlarmViewModel.onEvent(
                        AddEditAlarmScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                            isVisible = false
                        )
                    )
                    permissionsRequester.openApplicationSettings()
                }

                is AddEditAlarmScreenUserEvent.AdvancedSettingsClicked -> {
                    onAdvancedSettingsClicked()
                }

                is AddEditAlarmScreenUserEvent.SpecialSettingsClicked -> {
                    onSpecialSettingsClicked()
                }

                is AddEditAlarmScreenUserEvent.DownloadCode -> {
                    addEditAlarmViewModel.onEvent(
                        AddEditAlarmScreenUserEvent.DownloadCodeDialogVisible(isVisible = false)
                    )

                    try {
                        saveDefaultCodeImageLauncher.launch("QRAlarmCode.png")
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.issue_opening_the_page),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is AddEditAlarmScreenUserEvent.AlarmsChainSettingsClicked -> {
                    onAlarmsChainSettingsClicked()
                }

                else -> addEditAlarmViewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditAlarmScreenContent(
    state: AddEditAlarmState,
    onEvent: (AddEditAlarmScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.alarm),
                        style = MaterialTheme.typography.titleLarge
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
                        onClick = { onEvent(AddEditAlarmScreenUserEvent.SaveAlarmClicked) }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.Done,
                            tint = with(MaterialTheme) {
                                if (isQRAlarmTheme) colorScheme.onSurface
                                else LocalContentColor.current
                            },
                            contentDescription =
                                stringResource(R.string.content_description_done_icon)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = state.ui.isLoading,
            contentAlignment = Alignment.Center,
            label = "addEditAlarmScreenLoadingAnimation",
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (MaterialTheme.isQRAlarmTheme)
                        Modifier.background(
                            brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac))
                        )
                    else Modifier
                )
                .verticalScroll(rememberScrollState())
        ) { isLoading ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(paddingValues = paddingValues)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(MaterialTheme.space.xLarge)
                            .align(alignment = Alignment.Center)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(paddingValues = paddingValues)
                        .fillMaxWidth()
                ) {
                    if (state.draft.alarmHourOfDay != null && state.draft.alarmMinute != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MaterialTheme.space.mediumLarge)
                        ) {
                            QRAlarmTimePicker(
                                selectedHourOfDay = state.draft.alarmHourOfDay,
                                selectedMinute = state.draft.alarmMinute,
                                onTimeChanged = { hourOfDay, minute ->
                                    onEvent(
                                        AddEditAlarmScreenUserEvent.AlarmTimeChanged(
                                            newAlarmHourOfDay = hourOfDay,
                                            newAlarmMinute = minute,
                                            source = AddEditAlarmScreenUserEvent.AlarmTimeChanged.Source.Spinner
                                        )
                                    )
                                },
                                isEnabled = true,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            IconButton(
                                onClick = {
                                    onEvent(
                                        AddEditAlarmScreenUserEvent.DialerPickerDialogVisible(
                                            isVisible = true
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = MaterialTheme.space.xSmall)
                            ) {
                                Icon(
                                    imageVector = QRAlarmIcons.Clock,
                                    contentDescription =
                                        stringResource(R.string.content_description_clock_icon)
                                )
                            }
                        }
                    }

                    AlarmScheduleCard(
                        daysHoursAndMinutesUntilAlarm = state.ui.daysHoursAndMinutesUntilAlarm,
                        onlyOnceAlarmDateInMillis = state.draft.onlyOnceAlarmDateInMillis,
                        isOnlyOnce =
                            state.draft.alarmRepeatingScheduleWrapper.alarmRepeatingMode == ONLY_ONCE,
                        onAlarmDateClicked = {
                            onEvent(
                                AddEditAlarmScreenUserEvent.DatePickerDialogVisible(isVisible = true)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.mediumLarge
                            )
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.mediumLarge
                            )
                    ) {
                        ChoiceSetting(
                            onClick = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent
                                        .ChooseAlarmRepeatingScheduleDialogVisible(isVisible = true)
                                )
                            },
                            title = stringResource(R.string.repeating),
                            choiceName = getAlarmRepeatingScheduleString(
                                state.draft.alarmRepeatingScheduleWrapper
                            )
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = LocalContentColor.current,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )

                        ChoiceSetting(
                            onClick = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent
                                        .ChooseAlarmSnoozeConfigurationDialogVisible(isVisible = true)
                                )
                            },
                            title = stringResource(R.string.snooze),
                            choiceName = getAlarmSnoozeModeString(
                                state.draft.snoozeNumberToDurationPair
                            )
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = LocalContentColor.current,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )

                        ChoiceSetting(
                            onClick = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent
                                        .ChooseAlarmRingtoneDialogVisible(isVisible = true)
                                )
                            },
                            title = stringResource(R.string.ringtone),
                            choiceName = getAlarmRingtoneString(state.draft.ringtone)
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = LocalContentColor.current,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )

                        ToggleSetting(
                            isChecked = state.draft.areVibrationsEnabled,
                            onCheckedChange = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent.VibrationsEnabledChanged(
                                        areEnabled = it
                                    )
                                )
                            },
                            title = stringResource(R.string.vibrations),
                            compactHeight = true
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.mediumLarge
                            )
                    ) {
                        ToggleSetting(
                            isChecked = state.draft.isCodeEnabled,
                            onCheckedChange = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent.CodeEnabledChanged(isEnabled = it)
                                )
                            },
                            title = stringResource(R.string.use_qr_bar_code),
                            compactHeight = true
                        )

                        AnimatedVisibility(visible = state.draft.isCodeEnabled) {
                            Column {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = LocalContentColor.current,
                                    modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                                )

                                AnimatedContent(
                                    targetState = state.draft.currentlyAssignedCode != null ||
                                            state.draft.temporaryAssignedCode != null,
                                    label = "Code assigning animation"
                                ) { isAnyCodeAssigned ->
                                    Column {
                                        if (isAnyCodeAssigned) {
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
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            MaterialTheme.space.small
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = QRAlarmIcons.QrCode,
                                                            contentDescription = stringResource(
                                                                R.string.content_description_qr_code_icon
                                                            ),
                                                            modifier = Modifier.size(
                                                                MaterialTheme.space.mediumLarge
                                                            )
                                                        )

                                                        val effectiveCode =
                                                            state.draft.temporaryAssignedCode
                                                                ?: state.draft.currentlyAssignedCode

                                                        Text(
                                                            text = effectiveCode?.name
                                                                ?: stringResource(R.string.code_assigned),
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                    }

                                                    Text(
                                                        text = stringResource(
                                                            R.string.current_code,
                                                            (state.draft.temporaryAssignedCode?.value
                                                                ?: state.draft.currentlyAssignedCode?.value
                                                                ?: "").toBidiIsolated()
                                                        ),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(
                                                            top = MaterialTheme.space.xSmall
                                                        )
                                                    )
                                                }

                                                var expanded by remember { mutableStateOf(false) }

                                                IconButton(onClick = { expanded = true }) {
                                                    Icon(
                                                        imageVector = QRAlarmIcons.More,
                                                        contentDescription = stringResource(
                                                            R.string.content_description_more_icon
                                                        )
                                                    )

                                                    DropdownMenu(
                                                        expanded = expanded,
                                                        onDismissRequest = { expanded = false },
                                                        containerColor = with(MaterialTheme) {
                                                            if (isQRAlarmTheme) colorScheme.surfaceContainerHighest
                                                            else colorScheme.surfaceContainer
                                                        }
                                                    ) {
                                                        val areSavedCodesAvailable =
                                                            state.previouslySavedCodes.isNotEmpty()

                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    text = stringResource(
                                                                        R.string.edit_code_name
                                                                    ),
                                                                    style =
                                                                        MaterialTheme.typography.labelMedium
                                                                )
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    imageVector = QRAlarmIcons.Edit,
                                                                    contentDescription = stringResource(
                                                                        R.string.content_description_edit_icon
                                                                    )
                                                                )
                                                            },
                                                            onClick = {
                                                                expanded = false
                                                                onEvent(
                                                                    AddEditAlarmScreenUserEvent
                                                                        .EditCodeNameDialogVisible(
                                                                            isVisible = true
                                                                        )
                                                                )
                                                            },
                                                            colors = with(MaterialTheme) {
                                                                if (isQRAlarmTheme)
                                                                    MenuDefaults.itemColors(
                                                                        textColor = colorScheme.onSurface,
                                                                        leadingIconColor = colorScheme.onSurface
                                                                    )
                                                                else MenuDefaults.itemColors()
                                                            }
                                                        )

                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    text = stringResource(
                                                                        R.string.assign_new_code
                                                                    ),
                                                                    style =
                                                                        MaterialTheme.typography.labelMedium
                                                                )
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    imageVector =
                                                                        QRAlarmIcons.QrCodeScanner,
                                                                    contentDescription = stringResource(
                                                                        R.string.content_description_qr_code_scanner_icon
                                                                    )
                                                                )
                                                            },
                                                            onClick = {
                                                                expanded = false
                                                                onEvent(
                                                                    if (areSavedCodesAvailable) {
                                                                        AddEditAlarmScreenUserEvent
                                                                            .AssignCodeDialogVisible(
                                                                                isVisible = true
                                                                            )
                                                                    } else {
                                                                        AddEditAlarmScreenUserEvent
                                                                            .TryScanSpecificCode
                                                                    }
                                                                )
                                                            },
                                                            colors = with(MaterialTheme) {
                                                                if (isQRAlarmTheme)
                                                                    MenuDefaults.itemColors(
                                                                        textColor = colorScheme.onSurface,
                                                                        leadingIconColor = colorScheme.onSurface
                                                                    )
                                                                else MenuDefaults.itemColors()
                                                            }
                                                        )

                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    text = stringResource(
                                                                        R.string.clear_assigned_code
                                                                    ),
                                                                    style =
                                                                        MaterialTheme.typography.labelMedium
                                                                )
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    imageVector = QRAlarmIcons.Delete,
                                                                    contentDescription = stringResource(
                                                                        R.string.content_description_delete_icon
                                                                    )
                                                                )
                                                            },
                                                            onClick = {
                                                                expanded = false
                                                                onEvent(
                                                                    AddEditAlarmScreenUserEvent
                                                                        .ClearAssignedCode
                                                                )
                                                            },
                                                            colors = MenuDefaults.itemColors(
                                                                textColor = MaterialTheme.colorScheme.error,
                                                                leadingIconColor = MaterialTheme.colorScheme.error
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Column {
                                                Text(
                                                    text = stringResource(
                                                        R.string.assign_specific_code
                                                    ),
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
                                                    text = stringResource(
                                                        R.string.assign_specific_code_description
                                                    ),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier
                                                        .padding(
                                                            horizontal = MaterialTheme.space.medium
                                                        )
                                                )

                                                val areSavedCodesAvailable =
                                                    state.previouslySavedCodes.isNotEmpty()

                                                NavigationButton(
                                                    text = stringResource(R.string.scan_your_own_code),
                                                    onClick = {
                                                        onEvent(
                                                            if (areSavedCodesAvailable) {
                                                                AddEditAlarmScreenUserEvent
                                                                    .AssignCodeDialogVisible(
                                                                        isVisible = true
                                                                    )
                                                            } else {
                                                                AddEditAlarmScreenUserEvent
                                                                    .TryScanSpecificCode
                                                            }
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .padding(all = MaterialTheme.space.medium)
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = LocalContentColor.current,
                                            modifier = Modifier
                                                .padding(horizontal = MaterialTheme.space.medium)
                                        )

                                        SimpleSetting(
                                            onClick = {
                                                onEvent(
                                                    AddEditAlarmScreenUserEvent.AlarmsChainSettingsClicked
                                                )
                                            },
                                            title = stringResource(R.string.alarms_chain),
                                            description =
                                                pluralStringResource(R.plurals.alarms_active, 0, 0),
                                            icon = QRAlarmIcons.Chain,
                                            iconContentDescription = stringResource(
                                                R.string.content_description_chain_icon
                                            )
                                        )

                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = LocalContentColor.current,
                                            modifier = Modifier
                                                .padding(horizontal = MaterialTheme.space.medium)
                                        )

                                        SimpleSetting(
                                            onClick = {
                                                onEvent(
                                                    AddEditAlarmScreenUserEvent
                                                        .DownloadCodeDialogVisible(isVisible = true)
                                                )
                                            },
                                            title = stringResource(R.string.dont_have_code)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.mediumLarge
                            )
                    ) {
                        Column(modifier = Modifier.padding(all = MaterialTheme.space.medium)) {
                            Text(
                                text = stringResource(R.string.alarm_label),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(MaterialTheme.space.medium)
                            ) {
                                Icon(
                                    imageVector = QRAlarmIcons.Label,
                                    contentDescription = stringResource(
                                        R.string.content_description_label_icon
                                    ),
                                    modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                                )

                                val alarmLabelState =
                                    rememberTextFieldState(state.draft.alarmLabel ?: "")
                                val currentOnEvent by rememberUpdatedState(onEvent)

                                LaunchedEffect(alarmLabelState) {
                                    snapshotFlow { alarmLabelState.text.toString() }
                                        .drop(1)
                                        .collect { newAlarmLabel ->
                                            currentOnEvent(
                                                AddEditAlarmScreenUserEvent.AlarmLabelChanged(
                                                    newAlarmLabel = newAlarmLabel
                                                )
                                            )
                                        }
                                }

                                BasicTextField(
                                    state = alarmLabelState,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        color = LocalContentColor.current
                                    ),
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    decorator = { innerTextField ->
                                        if (alarmLabelState.text.isEmpty()) {
                                            Text(
                                                text =
                                                    stringResource(R.string.enter_your_alarm_label),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = LocalContentColor.current.copy(
                                                        alpha = 0.6f
                                                    )
                                                )
                                            )
                                        }

                                        innerTextField()
                                    },
                                    cursorBrush = SolidColor(LocalContentColor.current),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.mediumLarge
                            )
                    ) {
                        SimpleSetting(
                            onClick = {
                                onEvent(AddEditAlarmScreenUserEvent.AdvancedSettingsClicked)
                            },
                            title = stringResource(R.string.advanced_settings),
                            description = buildString {
                                append(stringResource(R.string.gentle_wake_up))
                                append(", ")
                                append(stringResource(R.string.temporary_mute))
                            }
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = LocalContentColor.current,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )

                        SimpleSetting(
                            onClick = {
                                onEvent(AddEditAlarmScreenUserEvent.SpecialSettingsClicked)
                            },
                            title = stringResource(R.string.special_settings),
                            description = buildString {
                                append(stringResource(R.string.do_not_leave_alarm))
                                append(", ")
                                append(stringResource(R.string.block_volume_down))
                            },
                            icon = QRAlarmIcons.Star,
                            iconContentDescription = stringResource(
                                R.string.content_description_star_icon
                            )
                        )
                    }

                    if (state.ui.isEditingExistingAlarm) {
                        OutlinedButton(
                            onClick = {
                                onEvent(
                                    AddEditAlarmScreenUserEvent.DeleteAlarmDialogVisible(
                                        isVisible = true
                                    )
                                )
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = MaterialTheme.space.medium,
                                    top = MaterialTheme.space.small,
                                    end = MaterialTheme.space.medium
                                )
                        ) {
                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(MaterialTheme.space.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = QRAlarmIcons.Delete,
                                    contentDescription = stringResource(
                                        R.string.content_description_delete_icon
                                    ),
                                    tint = MaterialTheme.colorScheme.error
                                )

                                Text(
                                    text = stringResource(R.string.delete_alarm),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.space.mediumLarge))
                }
            }
        }
    }

    if (state.ui.isDiscardAlarmChangesDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.discard_changes),
            message = stringResource(R.string.discard_changes_description),
            onDismissRequest = {
                onEvent(
                    AddEditAlarmScreenUserEvent.DiscardAlarmChangesDialogVisible(isVisible = false)
                )
            },
            onPositiveClick = {
                onEvent(
                    AddEditAlarmScreenUserEvent.DiscardAlarmChangesDialogVisible(isVisible = false)
                )
            },
            onNegativeClick = {
                onEvent(AddEditAlarmScreenUserEvent.ConfirmDiscardAlarmChanges)
            },
            positiveButtonText = stringResource(R.string.cancel),
            negativeButtonText = stringResource(R.string.discard)
        )
    }

    if (state.ui.isDialerPickerDialogVisible &&
        state.draft.alarmHourOfDay != null &&
        state.draft.alarmMinute != null
    ) {
        DialerTimePickerDialog(
            initialHourOfDay = state.draft.alarmHourOfDay,
            initialMinute = state.draft.alarmMinute,
            onDismissRequest = {
                onEvent(AddEditAlarmScreenUserEvent.DialerPickerDialogVisible(isVisible = false))
            },
            onTimeConfirmed = { hourOfDay, minute ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmTimeChanged(
                        newAlarmHourOfDay = hourOfDay,
                        newAlarmMinute = minute,
                        source = AddEditAlarmScreenUserEvent.AlarmTimeChanged.Source.Dialog
                    )
                )
            }
        )
    }

    if (state.ui.isDatePickerDialogVisible &&
        state.draft.alarmHourOfDay != null &&
        state.draft.alarmMinute != null
    ) {
        AlarmDatePickerDialog(
            initialDateInMillis = state.draft.onlyOnceAlarmDateInMillis,
            alarmHourOfDay = state.draft.alarmHourOfDay,
            alarmMinute = state.draft.alarmMinute,
            onDismissRequest = {
                onEvent(AddEditAlarmScreenUserEvent.DatePickerDialogVisible(isVisible = false))
            },
            onDateConfirmed = { selectedDateInMillis ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmDateSelected(
                        selectedDateInMillis = selectedDateInMillis
                    )
                )
            }
        )
    }

    if (state.ui.isChooseAlarmRepeatingScheduleDialogVisible) {
        ChooseAlarmRepeatingScheduleBottomSheet(
            initialAlarmRepeatingScheduleWrapper = state.draft.alarmRepeatingScheduleWrapper,
            onDismissRequest = { newAlarmRepeatingSchedule ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmRepeatingScheduleSelected(
                        newAlarmRepeatingScheduleWrapper = newAlarmRepeatingSchedule
                    )
                )
            }
        )
    }

    if (state.ui.isChooseAlarmSnoozeConfigurationDialogVisible) {
        ChooseSnoozeConfigurationBottomSheet(
            initialSnoozeNumberToDurationPair = state.draft.snoozeNumberToDurationPair,
            availableSnoozeNumbers = state.ui.availableSnoozeNumbers,
            availableSnoozeDurationsInMinutes = state.ui.availableSnoozeDurationsInMinutes,
            onDismissRequest = { newSnoozeNumberToDurationPair ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmSnoozeConfigurationSelected(
                        newSnoozeNumberToDurationPair = newSnoozeNumberToDurationPair
                    )
                )
            }
        )
    }

    if (state.ui.isChooseAlarmRingtoneConfigDialogVisible) {
        ChooseAlarmRingtoneConfigDialogBottomSheet(
            initialRingtone = state.draft.ringtone,
            availableRingtonesWithPlaybackState =
                state.availableRingtonesWithPlaybackState,
            isCustomRingtoneUploaded = state.draft.currentCustomAlarmRingtoneUri != null ||
                    state.draft.temporaryCustomAlarmRingtoneUri != null,
            onTogglePlaybackState = { toggledAlarmRingtone ->
                onEvent(
                    AddEditAlarmScreenUserEvent.ToggleAlarmRingtonePlayback(
                        ringtone = toggledAlarmRingtone
                    )
                )
            },
            onPickCustomRingtone = {
                onEvent(AddEditAlarmScreenUserEvent.PickCustomRingtone)
            },
            alarmVolumePercentage = state.draft.alarmVolumePercentage,
            onDismissRequest = { newAlarmRingtone, newAlarmVolumePercentage ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmRingtoneConfigSelected(
                        newRingtone = newAlarmRingtone,
                        newAlarmVolumePercentage = newAlarmVolumePercentage
                    )
                )
            }
        )
    }

    if (state.ui.isCameraPermissionDeniedDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.camera_permission_required),
            message = stringResource(R.string.camera_permission_required_description),
            onDismissRequest = {
                onEvent(
                    AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible(
                        isVisible = false
                    )
                )
            },
            onPositiveClick = {
                onEvent(AddEditAlarmScreenUserEvent.GoToApplicationSettingsClicked)
            },
            positiveButtonText = stringResource(R.string.settings),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }

    if (state.ui.isNotificationsPermissionDeniedDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.notifications_permission_required),
            message = stringResource(R.string.notifications_permission_required_description),
            onDismissRequest = {
                onEvent(
                    AddEditAlarmScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                        isVisible = false
                    )
                )
            },
            onPositiveClick = {
                onEvent(AddEditAlarmScreenUserEvent.GoToApplicationSettingsClicked)
            },
            positiveButtonText = stringResource(R.string.settings),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }

    if (state.ui.isAssignCodeDialogVisible) {
        AssignCodeBottomSheet(
            onScanCodeClicked = { onEvent(AddEditAlarmScreenUserEvent.TryScanSpecificCode) },
            availableCodes = state.previouslySavedCodes,
            onChooseCodeFromList = { code ->
                onEvent(AddEditAlarmScreenUserEvent.CodeChosenFromList(code = code))
            },
            onDismissRequest = {
                onEvent(AddEditAlarmScreenUserEvent.AssignCodeDialogVisible(isVisible = false))
            }
        )
    }

    if (state.ui.editCodeNameDialogState.isVisible) {
        EditCodeNameBottomSheet(
            initialCodeName = state.ui.editCodeNameDialogState.initialCodeName,
            onConfirmClicked = { newName ->
                onEvent(AddEditAlarmScreenUserEvent.CodeNameEdited(newName = newName))
            },
            onDismissRequest = {
                onEvent(AddEditAlarmScreenUserEvent.EditCodeNameDialogVisible(isVisible = false))
            }
        )
    }

    if (state.ui.permissionsDialogState.isVisible) {
        MissingPermissionsBottomSheet(
            permissionsDialogState = state.ui.permissionsDialogState,
            onPermissionClick = { onEvent(AddEditAlarmScreenUserEvent.RequestPermission(it)) },
            onAllPermissionsGranted = { onEvent(AddEditAlarmScreenUserEvent.SaveAlarmClicked) },
            onDismissRequest = { onEvent(AddEditAlarmScreenUserEvent.HideMissingPermissionsDialog) }
        )
    }

    if (state.ui.isDeleteAlarmDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.delete_this_alarm),
            onDismissRequest = {
                onEvent(AddEditAlarmScreenUserEvent.DeleteAlarmDialogVisible(isVisible = false))
            },
            onPositiveClick = {
                onEvent(AddEditAlarmScreenUserEvent.DeleteAlarm)
            },
            positiveButtonText = stringResource(R.string.delete),
            positiveButtonColor = MaterialTheme.colorScheme.error,
            negativeButtonText = stringResource(R.string.cancel)
        )
    }

    if (state.ui.isDownloadCodeDialogVisible) {
        DownloadCodeBottomSheet(
            onDownloadCodeClicked = {
                onEvent(AddEditAlarmScreenUserEvent.DownloadCode)
            },
            onDismissRequest = {
                onEvent(AddEditAlarmScreenUserEvent.DownloadCodeDialogVisible(isVisible = false))
            }
        )
    }
}

@Composable
private fun getAlarmSnoozeModeString(
    snoozeNumberToDurationPair: Pair<Int, Int>
): String {
    if (snoozeNumberToDurationPair.first == 0) {
        return stringResource(R.string.no_snoozes)
    }

    return pluralStringResource(
        R.plurals.snoozes_times_minutes_abbreviated,
        snoozeNumberToDurationPair.second,
        snoozeNumberToDurationPair.first,
        snoozeNumberToDurationPair.second
    )
}

@Composable
fun getAlarmRingtoneString(ringtone: Ringtone): String {
    return when (ringtone) {
        Ringtone.GENTLE_GUITAR -> stringResource(R.string.gentle_guitar)
        Ringtone.KALIMBA -> stringResource(R.string.kalimba)
        Ringtone.CLASSIC_ALARM -> stringResource(R.string.classic_alarm)
        Ringtone.ALARM_CLOCK -> stringResource(R.string.alarm_clock)
        Ringtone.ROOSTER -> stringResource(R.string.rooster)
        Ringtone.AIR_HORN -> stringResource(R.string.air_horn)
        Ringtone.SYSTEM_DEFAULT -> stringResource(R.string.system_default_ringtone)
        Ringtone.CUSTOM_SOUND -> stringResource(R.string.custom_sound)
    }
}

@Composable
fun getSecondsDurationString(durationInSeconds: Int): String {
    return if (durationInSeconds == 0) {
        stringResource(R.string.disabled)
    } else {
        pluralStringResource(R.plurals.seconds_abbreviated, durationInSeconds, durationInSeconds)
    }
}

@Composable
fun getCancelLockDurationString(durationInMinutes: Int): String {
    return when {
        durationInMinutes == 0 -> stringResource(R.string.disabled)
        durationInMinutes >= 60 && durationInMinutes % 60 == 0 -> {
            val hours = durationInMinutes / 60
            pluralStringResource(R.plurals.hours, hours, hours)
        }

        else -> {
            pluralStringResource(R.plurals.minutes, durationInMinutes, durationInMinutes)
        }
    }
}

@Composable
fun getCancelLockDurationAbbreviatedString(durationInMinutes: Int): String {
    return when {
        durationInMinutes == 0 -> stringResource(R.string.disabled)
        durationInMinutes >= 60 && durationInMinutes % 60 == 0 -> {
            val hours = durationInMinutes / 60
            pluralStringResource(R.plurals.hours_abbreviated, hours, hours)
        }

        else -> {
            pluralStringResource(
                R.plurals.minutes_abbreviated,
                durationInMinutes,
                durationInMinutes
            )
        }
    }
}

@Preview
@Composable
private fun AddEditAlarmScreenContentPreview() {
    QRAlarmTheme {
        AddEditAlarmScreenContent(
            state = AddEditAlarmState(
                draft = AlarmDraft(alarmHourOfDay = 8, alarmMinute = 0),
                ui = AddEditAlarmUiState(isLoading = false)
            ),
            onEvent = { }
        )
    }
}
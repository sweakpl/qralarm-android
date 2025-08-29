package com.sweak.qralarm.features.add_edit_alarm.destinations

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmCard
import com.sweak.qralarm.core.designsystem.component.QRAlarmDialog
import com.sweak.qralarm.core.designsystem.component.QRAlarmSwitch
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.ui.components.MissingPermissionsBottomSheet
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.core.ui.compose_util.getAlarmRepeatingScheduleString
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowBackendEvent
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowState
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AddEditAlarmScreenUserEvent
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmViewModel
import com.sweak.qralarm.features.add_edit_alarm.components.AssignCodeBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.ChooseAlarmRepeatingScheduleBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.ChooseAlarmRingtoneConfigDialogBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.ChooseSnoozeConfigurationBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.DialerTimePickerDialog
import com.sweak.qralarm.features.add_edit_alarm.components.DownloadCodeBottomSheet
import com.sweak.qralarm.features.add_edit_alarm.components.QRAlarmTimePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddEditAlarmScreen(
    addEditAlarmViewModel: AddEditAlarmViewModel,
    onCancelClicked: () -> Unit,
    onAlarmSaved: () -> Unit,
    onScanCustomCodeClicked: () -> Unit,
    onAdvancedSettingsClicked: () -> Unit,
    onSpecialSettingsClicked: () -> Unit,
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

    val saveDefaultCodeImageLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.CreateDocument("image/png") {},
        onResult = { uri ->
            coroutineScope.launch(Dispatchers.IO) {
                if (uri == null) return@launch

                try {
                    val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "w")

                    parcelFileDescriptor?.use {
                        val qrCodeImageBitmap =
                            BitmapFactory.decodeResource(context.resources, R.drawable.qr_code)
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
                                context.getString(R.string.saved_qr_code),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } ?: throw IOException()
                } catch (e: Exception) {
                    if (e is IOException || e is IllegalArgumentException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.could_not_save_qr_code),
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
    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )
    val notificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        object : PermissionState {
            override val permission: String get() = "android.permission.POST_NOTIFICATIONS"
            override val status: PermissionStatus get() = PermissionStatus.Granted
            override fun launchPermissionRequest() { /* no-op */ }
        }
    }

    ObserveAsEvents(
        flow = addEditAlarmViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is AddEditAlarmFlowBackendEvent.AlarmChangesDiscarded -> onCancelClicked()
                is AddEditAlarmFlowBackendEvent.AlarmSaved -> onAlarmSaved()
                is AddEditAlarmFlowBackendEvent.CustomRingtoneRetrievalFinished -> {
                    Toast.makeText(
                        context,
                        if (event.isSuccess) R.string.ringtone_successfully_uploaded
                        else R.string.ringtone_upload_unsuccessful,
                        Toast.LENGTH_LONG
                    ).show()
                }
                is AddEditAlarmFlowBackendEvent.CustomCodeAssignmentFinished -> {
                    Toast.makeText(
                        context,
                        R.string.code_successfully_assigned,
                        Toast.LENGTH_LONG
                    ).show()
                }
                is AddEditAlarmFlowBackendEvent.AlarmDeleted -> onAlarmDeleted()
                is AddEditAlarmFlowBackendEvent.AlarmRingtonePreviewPlaybackError -> {
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

            if (cameraPermissionState.status is PermissionStatus.Granted) {
                onScanCustomCodeClicked()
            }
        } else if (addEditAlarmScreenState.permissionsDialogState.isVisible) {
            addEditAlarmViewModel.onEvent(
                AddEditAlarmScreenUserEvent.TrySaveAlarm(
                    cameraPermissionStatus = cameraPermissionState.status.isGranted,
                    notificationsPermissionStatus =
                    notificationsPermissionState.status.isGranted
                )
            )
        }
    }

    BackHandler {
        addEditAlarmViewModel.onEvent(AddEditAlarmScreenUserEvent.OnCancelClicked)
    }

    AddEditAlarmScreenContent(
        state = addEditAlarmScreenState,
        onEvent = { event ->
            when (event) {
                is AddEditAlarmScreenUserEvent.ConfirmDiscardAlarmChanges -> onCancelClicked()
                is AddEditAlarmScreenUserEvent.SaveAlarmClicked -> {
                    addEditAlarmViewModel.onEvent(
                        AddEditAlarmScreenUserEvent.TrySaveAlarm(
                            cameraPermissionStatus = cameraPermissionState.status.isGranted,
                            notificationsPermissionStatus =
                            notificationsPermissionState.status.isGranted
                        )
                    )
                }
                is AddEditAlarmScreenUserEvent.RequestCameraPermission -> {
                    if (cameraPermissionState.status.shouldShowRationale) {
                        addEditAlarmViewModel.onEvent(
                            AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible(
                                isVisible = true
                            )
                        )
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
                is AddEditAlarmScreenUserEvent.RequestNotificationsPermission -> {
                    if (notificationsPermissionState.status.shouldShowRationale) {
                        addEditAlarmViewModel.onEvent(
                            AddEditAlarmScreenUserEvent.NotificationsPermissionDeniedDialogVisible(
                                isVisible = true
                            )
                        )
                    } else {
                        notificationsPermissionState.launchPermissionRequest()
                    }
                }
                is AddEditAlarmScreenUserEvent.RequestAlarmsPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                data = "package:${context.packageName}".toUri()
                            }
                        )
                    }
                }
                is AddEditAlarmScreenUserEvent.RequestFullScreenIntentPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
                                data = "package:${context.packageName}".toUri()
                            }
                        )
                    }
                }
                is AddEditAlarmScreenUserEvent.PickCustomRingtone -> {
                    try {
                        audioPickerLauncher.launch("audio/*")
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.issue_opening_the_page),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is AddEditAlarmScreenUserEvent.TryScanSpecificCode -> {
                    addEditAlarmViewModel.onEvent(
                        AddEditAlarmScreenUserEvent.AssignCodeDialogVisible(isVisible = false)
                    )

                    if (!cameraPermissionState.status.isGranted) {
                        isInTheCameraPermissionFlowForCustomCodeScan = true

                        if (cameraPermissionState.status.shouldShowRationale) {
                            addEditAlarmViewModel.onEvent(
                                AddEditAlarmScreenUserEvent.CameraPermissionDeniedDialogVisible(
                                    isVisible = true
                                )
                            )
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
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
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    )
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

                    saveDefaultCodeImageLauncher.launch("QRAlarmCode.png")
                }
                else -> addEditAlarmViewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditAlarmScreenContent(
    state: AddEditAlarmFlowState,
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
                        onClick = { onEvent(AddEditAlarmScreenUserEvent.SaveAlarmClicked) }
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
        AnimatedContent(
            targetState = state.isLoading,
            contentAlignment = Alignment.Center,
            label = "addEditAlarmScreenLoadingAnimation",
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
        ) { isLoading ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(paddingValues = paddingValues)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.tertiary,
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
                    if (state.alarmHourOfDay != null && state.alarmMinute != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MaterialTheme.space.mediumLarge)
                        ) {
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
                                        stringResource(R.string.content_description_clock_icon),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
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
                                            .ChooseAlarmRepeatingScheduleDialogVisible(
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
                                    text = stringResource(R.string.repeating),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f)
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
                                        contentDescription = stringResource(
                                            R.string.content_description_forward_arrow_icon
                                        ),
                                        modifier = Modifier.size(size = MaterialTheme.space.medium)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )

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
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = getAlarmSnoozeModeString(
                                            state.snoozeNumberToDurationPair
                                        ),
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

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )

                        Box(
                            modifier = Modifier
                                .clickable {
                                    onEvent(
                                        AddEditAlarmScreenUserEvent
                                            .ChooseAlarmRingtoneDialogVisible(isVisible = true)
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
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = getAlarmRingtoneString(state.ringtone),
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

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )

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
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f)
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
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f)
                            )

                            QRAlarmSwitch(
                                checked = state.isCodeEnabled,
                                onCheckedChange = {
                                    onEvent(
                                        AddEditAlarmScreenUserEvent.CodeEnabledChanged(
                                            isEnabled = it
                                        )
                                    )
                                }
                            )
                        }

                        AnimatedVisibility(visible = state.isCodeEnabled) {
                            Column {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                                )

                                AnimatedContent(
                                    targetState = state.currentlyAssignedCode != null ||
                                            state.temporaryAssignedCode != null,
                                    label = "Code assigning animation"
                                ) { isAnyCodeAssigned ->
                                    Column {
                                        if (isAnyCodeAssigned) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = stringResource(R.string.code_assigned),
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
                                                            R.string.current_code,
                                                            state.temporaryAssignedCode
                                                                ?: state.currentlyAssignedCode ?: ""
                                                        ),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier
                                                            .padding(
                                                                start = MaterialTheme.space.medium,
                                                                end = MaterialTheme.space.medium,
                                                                bottom = MaterialTheme.space.medium
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
                                                        modifier = Modifier
                                                            .wrapContentWidth()
                                                            .background(
                                                                color =
                                                                    MaterialTheme.colorScheme.surface
                                                            )
                                                    ) {
                                                        val areSavedCodesAvailable =
                                                            state.previouslySavedCodes.isNotEmpty()

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
                                                                    ),
                                                                    tint =
                                                                    MaterialTheme.colorScheme.onSurface
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
                                                            }
                                                        )

                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    text = stringResource(
                                                                        R.string.clear_assigned_code
                                                                    ),
                                                                    style =
                                                                    MaterialTheme.typography.labelMedium,
                                                                    color =
                                                                    MaterialTheme.colorScheme.error
                                                                )
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    imageVector = QRAlarmIcons.Delete,
                                                                    contentDescription = stringResource(
                                                                        R.string.content_description_delete_icon
                                                                    ),
                                                                    tint =
                                                                    MaterialTheme.colorScheme.error
                                                                )
                                                            },
                                                            onClick = {
                                                                expanded = false
                                                                onEvent(
                                                                    AddEditAlarmScreenUserEvent
                                                                        .ClearAssignedCode
                                                                )
                                                            }
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

                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor =
                                                        MaterialTheme.colorScheme.secondary
                                                    ),
                                                    modifier = Modifier
                                                        .padding(all = MaterialTheme.space.medium)
                                                        .clickable {
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
                                                        }
                                                ) {
                                                    Row(
                                                        horizontalArrangement =
                                                        Arrangement.SpaceBetween,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(
                                                                all = MaterialTheme.space.medium
                                                            )
                                                    ) {
                                                        Text(
                                                            text = stringResource(
                                                                if (areSavedCodesAvailable) {
                                                                    R.string.assign_specific_code
                                                                } else {
                                                                    R.string.scan_your_own_code
                                                                }
                                                            ),
                                                            style =
                                                            MaterialTheme.typography.labelLarge
                                                        )

                                                        Icon(
                                                            imageVector = QRAlarmIcons.ForwardArrow,
                                                            contentDescription = stringResource(
                                                                R.string.content_description_forward_arrow_icon
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(horizontal = MaterialTheme.space.medium)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clickable {
                                                    onEvent(
                                                        AddEditAlarmScreenUserEvent
                                                            .DownloadCodeDialogVisible(
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
                                                    text = stringResource(R.string.dont_have_code),
                                                    style = MaterialTheme.typography.titleLarge
                                                )

                                                Icon(
                                                    imageVector = QRAlarmIcons.ForwardArrow,
                                                    contentDescription = stringResource(
                                                        R.string.content_description_forward_arrow_icon
                                                    ),
                                                    modifier = Modifier
                                                        .size(
                                                            size = MaterialTheme.space.mediumLarge
                                                        )
                                                )
                                            }
                                        }
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

                                BasicTextField(
                                    value = state.alarmLabel ?: "",
                                    onValueChange = {
                                        onEvent(
                                            AddEditAlarmScreenUserEvent.AlarmLabelChanged(
                                                newAlarmLabel = it
                                            )
                                        )
                                    },
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onTertiary
                                    ),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (state.alarmLabel.isNullOrBlank()) {
                                            Text(
                                                text =
                                                stringResource(R.string.enter_your_alarm_label),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color =
                                                    MaterialTheme.colorScheme.onTertiary.copy(
                                                        alpha = 0.25f
                                                    )
                                                )
                                            )
                                        }

                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                                    onEvent(AddEditAlarmScreenUserEvent.AdvancedSettingsClicked)
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
                                        text = stringResource(R.string.advanced_settings),
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier
                                            .padding(bottom = MaterialTheme.space.xSmall)
                                    )

                                    Text(
                                        text = buildString {
                                            append(stringResource(R.string.gentle_wake_up))
                                            append(", ")
                                            append(stringResource(R.string.temporary_mute))
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Icon(
                                    imageVector = QRAlarmIcons.ForwardArrow,
                                    contentDescription = stringResource(
                                        R.string.content_description_forward_arrow_icon
                                    ),
                                    modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                                )
                            }
                        }

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )

                        Box(
                            modifier = Modifier
                                .clickable {
                                    onEvent(AddEditAlarmScreenUserEvent.SpecialSettingsClicked)
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
                                    Row(
                                        modifier = Modifier
                                            .padding(bottom = MaterialTheme.space.xSmall)
                                    ) {
                                        Icon(
                                            imageVector = QRAlarmIcons.Star,
                                            contentDescription = stringResource(
                                                R.string.content_description_star_icon
                                            ),
                                            modifier = Modifier
                                                .size(size = MaterialTheme.space.mediumLarge)
                                        )

                                        Text(
                                            text = stringResource(R.string.special_settings),
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier
                                                .padding(start = MaterialTheme.space.small)
                                        )
                                    }

                                    Text(
                                        text = buildString {
                                            append(stringResource(R.string.do_not_leave_alarm))
                                            append(", ")
                                            append(stringResource(R.string.block_volume_down))
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Icon(
                                    imageVector = QRAlarmIcons.ForwardArrow,
                                    contentDescription = stringResource(
                                        R.string.content_description_forward_arrow_icon
                                    ),
                                    modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                                )
                            }
                        }
                    }

                    if (state.isEditingExistingAlarm) {
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

    if (state.isDiscardAlarmChangesDialogVisible) {
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

    if (state.isDialerPickerDialogVisible &&
        state.alarmHourOfDay != null &&
        state.alarmMinute != null
    ) {
        DialerTimePickerDialog(
            initialHourOfDay = state.alarmHourOfDay,
            initialMinute = state.alarmMinute,
            onDismissRequest = {
                onEvent(AddEditAlarmScreenUserEvent.DialerPickerDialogVisible(isVisible = false))
            },
            onTimeConfirmed = { hourOfDay, minute ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmTimeChanged(
                        newAlarmHourOfDay = hourOfDay,
                        newAlarmMinute = minute
                    )
                )
            }
        )
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
            initialSnoozeNumberToDurationPair = state.snoozeNumberToDurationPair,
            availableSnoozeNumbers = state.availableSnoozeNumbers,
            availableSnoozeDurationsInMinutes = state.availableSnoozeDurationsInMinutes,
            onDismissRequest = { newSnoozeNumberToDurationPair ->
                onEvent(
                    AddEditAlarmScreenUserEvent.AlarmSnoozeConfigurationSelected(
                        newSnoozeNumberToDurationPair = newSnoozeNumberToDurationPair
                    )
                )
            }
        )
    }

    if (state.isChooseAlarmRingtoneConfigDialogVisible) {
        ChooseAlarmRingtoneConfigDialogBottomSheet(
            initialRingtone = state.ringtone,
            availableRingtonesWithPlaybackState =
            state.availableRingtonesWithPlaybackState,
            isCustomRingtoneUploaded = state.currentCustomAlarmRingtoneUri != null ||
                    state.temporaryCustomAlarmRingtoneUri != null,
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
            alarmVolumePercentage = state.alarmVolumePercentage,
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

    if (state.isCameraPermissionDeniedDialogVisible) {
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

    if (state.isNotificationsPermissionDeniedDialogVisible) {
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

    if (state.isAssignCodeDialogVisible) {
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

    if (state.permissionsDialogState.isVisible) {
        MissingPermissionsBottomSheet(
            cameraPermissionState = state.permissionsDialogState.cameraPermissionState,
            onCameraPermissionClick = {
                onEvent(AddEditAlarmScreenUserEvent.RequestCameraPermission)
            },
            alarmsPermissionState = state.permissionsDialogState.alarmsPermissionState,
            onAlarmsPermissionClick = {
                onEvent(AddEditAlarmScreenUserEvent.RequestAlarmsPermission)
            },
            notificationsPermissionState = state.permissionsDialogState.notificationsPermissionState,
            onNotificationsPermissionClick = {
                onEvent(AddEditAlarmScreenUserEvent.RequestNotificationsPermission)
            },
            fullScreenIntentPermissionState =
            state.permissionsDialogState.fullScreenIntentPermissionState,
            onFullScreenIntentPermissionClick = {
                onEvent(AddEditAlarmScreenUserEvent.RequestFullScreenIntentPermission)
            },
            onAllPermissionsGranted = { onEvent(AddEditAlarmScreenUserEvent.SaveAlarmClicked) },
            onDismissRequest = { onEvent(AddEditAlarmScreenUserEvent.HideMissingPermissionsDialog) }
        )
    }

    if (state.isDeleteAlarmDialogVisible) {
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

    if (state.isDownloadCodeDialogVisible) {
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

    return snoozeNumberToDurationPair.first.toString() +
            " x " +
            snoozeNumberToDurationPair.second +
            " " + stringResource(R.string.min)
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
        Ringtone.CUSTOM_SOUND -> stringResource(R.string.custom_sound)
    }
}

@Composable
fun getSecondsDurationString(durationInSeconds: Int): String {
    return if (durationInSeconds == 0) {
        stringResource(R.string.disabled)
    } else {
        durationInSeconds.toString() + " " + stringResource(R.string.sec)
    }
}

@Preview
@Composable
private fun AddEditAlarmScreenContentPreview() {
    QRAlarmTheme {
        AddEditAlarmScreenContent(
            state = AddEditAlarmFlowState(
                isLoading = false,
                alarmHourOfDay = 8,
                alarmMinute = 0
            ),
            onEvent = { }
        )
    }
}
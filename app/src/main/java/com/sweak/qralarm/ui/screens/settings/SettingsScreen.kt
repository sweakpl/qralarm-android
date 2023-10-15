package com.sweak.qralarm.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.shared.components.*
import com.sweak.qralarm.ui.screens.shared.navigateThrottled
import com.sweak.qralarm.ui.theme.Kimberly
import com.sweak.qralarm.ui.theme.space
import com.sweak.qralarm.util.SCAN_MODE_SET_CUSTOM_CODE
import com.sweak.qralarm.util.Screen

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    context: Context = LocalContext.current
) {
    val uiState = remember { settingsViewModel.settingsUiState }
    val storagePermissionState = rememberPermissionState(
        permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )
    val scrollState = rememberScrollState()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.GetContent() {
            override fun createIntent(context: Context, input: String): Intent {
                return super.createIntent(context, input).apply {
                    putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                }
            }
        },
        onResult = { uri -> settingsViewModel.updateLocalAlarmSoundSelection(uri, context) }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                settingsViewModel.stopMediaPlayer()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val constraints = ConstraintSet {
        val backButton = createRefFor("backButton")
        val settingsText = createRefFor("settingsText")
        val settingsColumn = createRefFor("settingsColumn")

        constrain(backButton) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }

        constrain(settingsText) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(settingsColumn) {
            top.linkTo(settingsText.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
        }
    }

    ConstraintLayout(
        constraints,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        BackButton(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.space.medium,
                    top = MaterialTheme.space.large - MaterialTheme.space.extraSmall
                )
                .layoutId("backButton"),
            navController = navController
        )

        Text(
            text = stringResource(R.string.settings),
            modifier = Modifier
                .padding(
                    MaterialTheme.space.small,
                    MaterialTheme.space.large,
                    MaterialTheme.space.small,
                    MaterialTheme.space.large
                )
                .layoutId("settingsText"),
            style = MaterialTheme.typography.displayLarge
        )

        Column(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.space.large)
                .verticalScroll(scrollState)
                .layoutId("settingsColumn"),
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(bottom = MaterialTheme.space.medium),
                    text = stringResource(R.string.alarm_sound),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Normal
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ComboBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(end = MaterialTheme.space.medium),
                        menuItems = uiState.value.availableAlarmSounds.map {
                            stringResource(it.nameResourceId)
                        },
                        menuExpandedState = uiState.value.alarmSoundsDropdownMenuExpanded,
                        selectedIndex = uiState.value.selectedAlarmSoundIndex,
                        updateMenuExpandedStatus = {
                            uiState.value =
                                uiState.value.copy(alarmSoundsDropdownMenuExpanded = true)
                        },
                        onDismissMenuView = {
                            uiState.value =
                                uiState.value.copy(alarmSoundsDropdownMenuExpanded = false)
                        },
                        onMenuItemClick = { index ->
                            if (settingsViewModel.isLocalSoundAlarmChosen(index)) {
                                audioPickerLauncher.launch("audio/*")
                            } else {
                                settingsViewModel.updateAlarmSoundSelection(index)
                            }
                            uiState.value =
                                uiState.value.copy(alarmSoundsDropdownMenuExpanded = false)
                        }
                    )

                    IconButton(
                        onClick = {
                            settingsViewModel.playOrStopAlarmPreview(context)
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (uiState.value.alarmPreviewPlaying) {
                                    R.drawable.ic_stop
                                } else {
                                    R.drawable.ic_play
                                }
                            ),
                            contentDescription = "Play/Stop button",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.snooze_duration_setting_title),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.snooze_duration_setting_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                ComboBox(
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),
                    enabled = uiState.value.availableSnoozeMaxCounts
                            [uiState.value.selectedSnoozeMaxCountIndex].count != 0,
                    menuItems = uiState.value.availableSnoozeDurations,
                    menuExpandedState = uiState.value.snoozeDurationsDropdownMenuExpanded,
                    selectedIndex = uiState.value.selectedSnoozeDurationIndex,
                    updateMenuExpandedStatus = {
                        uiState.value =
                            uiState.value.copy(snoozeDurationsDropdownMenuExpanded = true)
                    },
                    onDismissMenuView = {
                        uiState.value =
                            uiState.value.copy(snoozeDurationsDropdownMenuExpanded = false)
                    },
                    onMenuItemClick = { index ->
                        settingsViewModel.updateSnoozeDurationSelection(index)
                        uiState.value =
                            uiState.value.copy(snoozeDurationsDropdownMenuExpanded = false)
                    }
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.snooze_number_setting_title),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.snooze_number_setting_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                ComboBox(
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),
                    menuItems = uiState.value.availableSnoozeMaxCounts,
                    menuExpandedState = uiState.value.snoozeMaxCountsDropdownMenuExpanded,
                    selectedIndex = uiState.value.selectedSnoozeMaxCountIndex,
                    updateMenuExpandedStatus = {
                        uiState.value =
                            uiState.value.copy(snoozeMaxCountsDropdownMenuExpanded = true)
                    },
                    onDismissMenuView = {
                        uiState.value =
                            uiState.value.copy(snoozeMaxCountsDropdownMenuExpanded = false)
                    },
                    onMenuItemClick = { index ->
                        settingsViewModel.updateSnoozeMaxCountSelection(index)
                        uiState.value =
                            uiState.value.copy(snoozeMaxCountsDropdownMenuExpanded = false)
                    }
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.gentle_wakeup_setting_title),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.gentle_wakeup_setting_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                ComboBox(
                    modifier = Modifier
                        .height(40.dp)
                        .width(80.dp),
                    menuItems = uiState.value.availableGentleWakeupDurations,
                    menuExpandedState =
                    uiState.value.availableGentleWakeupDurationsDropdownMenuExpanded,
                    selectedIndex = uiState.value.selectedGentleWakeupDurationIndex,
                    updateMenuExpandedStatus = {
                        uiState.value =
                            uiState.value.copy(
                                availableGentleWakeupDurationsDropdownMenuExpanded = true
                            )
                    },
                    onDismissMenuView = {
                        uiState.value =
                            uiState.value.copy(
                                availableGentleWakeupDurationsDropdownMenuExpanded = false
                            )
                    },
                    onMenuItemClick = { index ->
                        settingsViewModel.updateGentleWakeupDurationSelection(index)
                        uiState.value =
                            uiState.value.copy(
                                availableGentleWakeupDurationsDropdownMenuExpanded = false
                            )
                    }
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.enable_vibrations_setting_title),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.enable_vibrations_setting_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    Switch(
                        checked = uiState.value.vibrationsEnabled,
                        onCheckedChange = settingsViewModel::handleEnableVibrationsSwitch,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor =
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                            uncheckedTrackColor =
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .padding(
                                horizontal = 20.dp,
                                vertical = MaterialTheme.space.medium
                            )
                            .scale(1.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.accept_all_codes_setting_title),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.accept_all_codes_setting_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    Switch(
                        checked = uiState.value.acceptAnyCodeType,
                        onCheckedChange = settingsViewModel::handleAcceptBarcodesSwitch,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor =
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                            uncheckedTrackColor =
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .padding(
                                horizontal = 20.dp,
                                vertical = MaterialTheme.space.medium
                            )
                            .scale(1.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.no_code_cancel_setting_title),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.no_code_cancel_setting_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    Switch(
                        checked = uiState.value.noCodeCancelEnabled,
                        onCheckedChange = settingsViewModel::handleNoCodeCancelSwitch,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor =
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                            uncheckedTrackColor =
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .padding(
                                horizontal = 20.dp,
                                vertical = MaterialTheme.space.medium
                            )
                            .scale(1.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.download_code_title),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Normal)
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.download_code_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                IconButton(
                    onClick = {
                        settingsViewModel.handleDefaultCodeDownloadButton(
                            context,
                            storagePermissionState
                        )
                    },
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_download),
                        contentDescription = "Download button",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.scan_new_code_title),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.scan_new_code_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                IconButton(
                    onClick = {
                        settingsViewModel.handleScanCustomDismissCodeButton(
                            navController,
                            cameraPermissionState,
                            lifecycleOwner
                        )
                    },
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scan),
                        contentDescription = "Scan button",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.qralarm_guide_title),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.qralarm_guide_description),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                IconButton(
                    onClick = {
                        navController.navigateThrottled(
                            Screen.GuideScreen.route,
                            lifecycleOwner
                        )
                    },
                    modifier = Modifier
                        .padding(end = MaterialTheme.space.medium)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_guide),
                        contentDescription = "Guide button",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.space.large))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(
                    R.string.current_dismiss_code,
                    uiState.value.dismissAlarmCode
                ),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(MaterialTheme.space.large))
        }
    }

    StoragePermissionDialog(
        uiState = uiState,
        onPositiveClick = {
            storagePermissionState.launchPermissionRequest()
            uiState.value = uiState.value.copy(showStoragePermissionDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showStoragePermissionDialog = false)
        }
    )

    StoragePermissionRevokedDialog(
        uiState = uiState,
        onPositiveClick = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
            uiState.value = uiState.value.copy(showStoragePermissionRevokedDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showStoragePermissionRevokedDialog = false)
        }
    )

    DismissCodeAddedDialog(
        uiState = uiState,
        onPositiveClick = {
            uiState.value = uiState.value.copy(showDismissCodeAddedDialog = false)
        },
        onNegativeClick = {
            navController.navigateThrottled(
                Screen.ScannerScreen.withArguments(SCAN_MODE_SET_CUSTOM_CODE),
                lifecycleOwner
            )
            uiState.value = uiState.value.copy(showDismissCodeAddedDialog = false)
        }
    )

    CameraPermissionAddCodeDialog(
        uiState = uiState,
        onPositiveClick = {
            cameraPermissionState.launchPermissionRequest()
            uiState.value = uiState.value.copy(showCameraPermissionDialog = false)
        },
        onNegativeClick = { uiState.value = uiState.value.copy(showCameraPermissionDialog = false) }
    )

    CameraPermissionAddCodeRevokedDialog(
        uiState = uiState,
        onPositiveClick = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
            uiState.value = uiState.value.copy(showCameraPermissionRevokedDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showCameraPermissionRevokedDialog = false)
        }
    )

    DisablingBarcodesSupportDialog(
        uiState = uiState,
        onPositiveClick = {
            settingsViewModel.disableBarcodesSupport()
            uiState.value = uiState.value.copy(showDisablingBarcodesSupportDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showDisablingBarcodesSupportDialog = false)
        }
    )
}

@Composable
fun ComboBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuItems: List<Any>,
    menuExpandedState: Boolean,
    selectedIndex: Int,
    updateMenuExpandedStatus: () -> Unit,
    onDismissMenuView: () -> Unit,
    onMenuItemClick: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (enabled) MaterialTheme.colorScheme.tertiary else Kimberly,
                shape = RoundedCornerShape(4.dp)
            )
            .run {
                if (enabled) {
                    clickable(onClick = updateMenuExpandedStatus)
                } else {
                    this
                }
            }
    ) {
        val constraints = ConstraintSet {
            val selectionLabel = createRefFor("selectionLabel")
            val arrowIcon = createRefFor("arrowIcon")

            constrain(selectionLabel) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(arrowIcon.start)
                width = Dimension.fillToConstraints
            }

            constrain(arrowIcon) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        }

        ConstraintLayout(
            constraints,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .layoutId("selectionLabel")
                    .padding(
                        start = MaterialTheme.space.medium,
                        top = MaterialTheme.space.extraSmall,
                        bottom = MaterialTheme.space.extraSmall
                    )
                    .wrapContentHeight(),
                text = menuItems[selectedIndex].toString(),
                style = MaterialTheme.typography.displayMedium
            )

            Icon(
                modifier = Modifier
                    .layoutId("arrowIcon")
                    .size(24.dp, 20.dp)
                    .padding(end = MaterialTheme.space.small),
                painter = painterResource(R.drawable.ic_dropdown_arrow),
                contentDescription = "Dropdown menu icon",
                tint = Color.White
            )

            DropdownMenu(
                expanded = menuExpandedState,
                onDismissRequest = onDismissMenuView,
                offset = DpOffset(
                    MaterialTheme.space.extraSmall,
                    -MaterialTheme.space.extraSmall
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                menuItems.forEachIndexed { index, content ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = content.toString(),
                                style = MaterialTheme.typography.displayMedium
                            )
                        },
                        onClick = { onMenuItemClick(index) }
                    )
                }
            }
        }
    }
}

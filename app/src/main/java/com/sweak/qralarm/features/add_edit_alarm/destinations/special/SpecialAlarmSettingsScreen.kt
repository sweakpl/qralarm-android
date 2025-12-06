package com.sweak.qralarm.features.add_edit_alarm.destinations.special

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmCard
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.SpecialAlarmSettingsScreenUserEvent
import com.sweak.qralarm.features.add_edit_alarm.components.ToggleSetting

@Composable
fun SpecialAlarmSettingsScreen(
    onCancelClicked: () -> Unit,
    onRedirectToQRAlarmPro: () -> Unit
) {
    SpecialAlarmSettingsScreenContent(
        onEvent = { event ->
            when (event) {
                is SpecialAlarmSettingsScreenUserEvent.OnCancelClicked -> {
                    onCancelClicked()
                }
                is SpecialAlarmSettingsScreenUserEvent.TryUseSpecialAlarmSettings -> {
                    onRedirectToQRAlarmPro()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialAlarmSettingsScreenContent(
    onEvent: (SpecialAlarmSettingsScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.special_settings),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(SpecialAlarmSettingsScreenUserEvent.OnCancelClicked) }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.BackArrow,
                            contentDescription =
                                stringResource(R.string.content_description_back_arrow_icon)
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
            Column(modifier = Modifier.padding(paddingValues)) {
                QRAlarmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.space.medium,
                            vertical = MaterialTheme.space.mediumLarge
                        )
                ) {
                    ToggleSetting(
                        isChecked = false,
                        onCheckedChange = {
                            onEvent(SpecialAlarmSettingsScreenUserEvent.TryUseSpecialAlarmSettings)
                        },
                        title = stringResource(R.string.do_not_leave_alarm),
                        description = stringResource(R.string.do_not_leave_alarm_description)
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    )

                    ToggleSetting(
                        isChecked = false,
                        onCheckedChange = {
                            onEvent(SpecialAlarmSettingsScreenUserEvent.TryUseSpecialAlarmSettings)
                        },
                        title = stringResource(R.string.power_off_guard),
                        description = stringResource(R.string.power_off_guard_description)
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    )

                    ToggleSetting(
                        isChecked = false,
                        onCheckedChange = {
                            onEvent(SpecialAlarmSettingsScreenUserEvent.TryUseSpecialAlarmSettings)
                        },
                        title = stringResource(R.string.block_volume_down),
                        description = stringResource(R.string.block_volume_down_description)
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    )

                    ToggleSetting(
                        isChecked = false,
                        onCheckedChange = {
                            onEvent(SpecialAlarmSettingsScreenUserEvent.TryUseSpecialAlarmSettings)
                        },
                        title = stringResource(R.string.keep_ringer_on),
                        description = stringResource(R.string.keep_ringer_on_description)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SpecialAlarmSettingsScreenContentPreview() {
    QRAlarmTheme {
        SpecialAlarmSettingsScreenContent(
            onEvent = {}
        )
    }
}
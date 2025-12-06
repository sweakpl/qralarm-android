package com.sweak.qralarm.features.add_edit_alarm.destinations.alarms_chain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowState
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmFlowUserEvent.AlarmsChainSettingsScreenUserEvent
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmViewModel
import com.sweak.qralarm.features.add_edit_alarm.destinations.alarms_chain.components.AddNewChainedAlarmCard
import com.sweak.qralarm.features.add_edit_alarm.destinations.alarms_chain.components.OriginalAlarmCard

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AlarmsChainSettingsScreen(
    addEditAlarmViewModel: AddEditAlarmViewModel,
    onCancelClicked: () -> Unit,
    onRedirectToQRAlarmPro: () -> Unit
) {
    val addEditAlarmScreenState by addEditAlarmViewModel.state.collectAsStateWithLifecycle()

    AlarmsChainSettingsScreenContent(
        state = addEditAlarmScreenState,
        onEvent = { event ->
            when (event) {
                is AlarmsChainSettingsScreenUserEvent.OnCancelClicked -> {
                    onCancelClicked()
                }
                is AlarmsChainSettingsScreenUserEvent.AddNewChainedAlarmClicked -> {
                    onRedirectToQRAlarmPro()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsChainSettingsScreenContent(
    state: AddEditAlarmFlowState,
    onEvent: (AlarmsChainSettingsScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.alarms_chain),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(AlarmsChainSettingsScreenUserEvent.OnCancelClicked) }
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
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxWidth()
            ) {
                item(key = "alarms_chain_description") {
                    Text(
                        text = stringResource(R.string.alarms_chain_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(
                                start = MaterialTheme.space.medium,
                                top = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.large
                            )
                    )
                }

                item(key = "original_alarm_card") {
                    if (state.alarmHourOfDay != null && state.alarmMinute != null) {
                        OriginalAlarmCard(
                            alarmHourOfDay = state.alarmHourOfDay,
                            alarmMinute = state.alarmMinute,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                        )
                    }
                }

                item(key = "add_new_chained_alarm_card") {
                    AddNewChainedAlarmCard(
                        onAddNewChainedAlarmClicked = {
                            onEvent(AlarmsChainSettingsScreenUserEvent.AddNewChainedAlarmClicked)
                        },
                        modifier = Modifier
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.medium
                            )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlarmsChainSettingsScreenContentPreview() {
    QRAlarmTheme {
        AlarmsChainSettingsScreenContent(
            state = AddEditAlarmFlowState(
                isLoading = false,
                alarmHourOfDay = 7,
                alarmMinute = 30,
            ),
            onEvent = {}
        )
    }
}
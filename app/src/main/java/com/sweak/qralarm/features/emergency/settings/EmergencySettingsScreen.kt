package com.sweak.qralarm.features.emergency.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.sweak.qralarm.core.designsystem.component.QRAlarmComboBox
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_AVAILABLE_REQUIRED_MATCHES
import com.sweak.qralarm.features.emergency.settings.util.EMERGENCY_AVAILABLE_SLIDER_RANGES

@Composable
fun EmergencySettingsScreen(
    onBackClicked: () -> Unit,
    onPreviewEmergencyTaskClicked: () -> Unit
) {
    val emergencySettingsViewModel = hiltViewModel<EmergencySettingsViewModel>()
    val emergencySettingsScreenState by
        emergencySettingsViewModel.state.collectAsStateWithLifecycle()

    EmergencySettingsScreenContent(
        state = emergencySettingsScreenState,
        onEvent = { event ->
            when (event) {
                is EmergencySettingsScreenUserEvent.BackClicked -> {
                    onBackClicked()
                }
                is EmergencySettingsScreenUserEvent.PreviewEmergencyTaskClicked -> {
                    onPreviewEmergencyTaskClicked()
                }
                else -> emergencySettingsViewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencySettingsScreenContent(
    state: EmergencySettingsScreenState,
    onEvent: (EmergencySettingsScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.emergency_task),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(EmergencySettingsScreenUserEvent.BackClicked) }
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
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
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.emergency_task_global_settings_description),
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

                QRAlarmCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.medium
                        )
                ) {
                    Text(
                        text = stringResource(R.string.slider_range),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(
                                start = MaterialTheme.space.medium,
                                top = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.small
                            )
                    )

                    if (state.selectedSliderRangeIndex != null) {
                        QRAlarmComboBox(
                            menuItems = state.availableSliderRanges.map {
                                "${it.first} - ${it.last}"
                            },
                            selectedIndex = state.selectedSliderRangeIndex,
                            onMenuItemClick = { index ->
                                onEvent(EmergencySettingsScreenUserEvent.SliderRangeSelected(index))
                            },
                            modifier = Modifier
                                .padding(
                                    start = MaterialTheme.space.medium,
                                    end = MaterialTheme.space.medium,
                                    bottom = MaterialTheme.space.medium
                                )
                        )
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.smallMedium)
                    )

                    Text(
                        text = stringResource(R.string.required_matches),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(
                                start = MaterialTheme.space.medium,
                                top = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.small
                            )
                    )

                    if (state.selectedRequiredMatchesIndex != null) {
                        QRAlarmComboBox(
                            menuItems = state.availableRequiredMatches,
                            selectedIndex = state.selectedRequiredMatchesIndex,
                            onMenuItemClick = { index ->
                                onEvent(
                                    EmergencySettingsScreenUserEvent.RequiredMatchesSelected(index)
                                )
                            },
                            modifier = Modifier
                                .padding(
                                    start = MaterialTheme.space.medium,
                                    end = MaterialTheme.space.medium,
                                    bottom = MaterialTheme.space.medium
                                )
                        )
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.smallMedium)
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .padding(all = MaterialTheme.space.medium)
                            .clickable {
                                onEvent(
                                    EmergencySettingsScreenUserEvent.PreviewEmergencyTaskClicked
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
                                text = stringResource(R.string.preview_emergency_task),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
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
        }
    }
}

@Preview
@Composable
fun EmergencySettingsContentPreview() {
    QRAlarmTheme {
        EmergencySettingsScreenContent(
            state = EmergencySettingsScreenState(
                availableSliderRanges = EMERGENCY_AVAILABLE_SLIDER_RANGES,
                selectedSliderRangeIndex = 2,
                availableRequiredMatches = EMERGENCY_AVAILABLE_REQUIRED_MATCHES,
                selectedRequiredMatchesIndex = 1
            ),
            onEvent = {}
        )
    }
}
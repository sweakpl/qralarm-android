package com.sweak.qralarm.features.emergency.task

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.service.AlarmService.Companion.ACTION_EMERGENCY_TASK_ALARM_MUTE
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.features.emergency.task.components.AlarmMuteIndicator
import com.sweak.qralarm.features.emergency.task.components.EmergencyInfoCard
import com.sweak.qralarm.features.emergency.task.components.EmergencyTaskCard

@Composable
fun EmergencyScreen(
    onCloseClicked: () -> Unit,
    onEmergencyTaskCompleted: () -> Unit
) {
    val emergencyViewModel = hiltViewModel<EmergencyViewModel>()
    val emergencyScreenState by emergencyViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(
        flow = emergencyViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is EmergencyScreenBackendEvent.TaskValueMatched -> {
                    context.sendBroadcast(
                        Intent(ACTION_EMERGENCY_TASK_ALARM_MUTE).apply {
                            setPackage(context.packageName)
                        }
                    )
                }
                is EmergencyScreenBackendEvent.EmergencyTaskCompleted -> onEmergencyTaskCompleted()
            }
        }
    )

    EmergencyScreenContent(
        state = emergencyScreenState,
        onEvent = { event ->
            when (event) {
                is EmergencyScreenUserEvent.OnCloseClicked -> onCloseClicked()
                else -> emergencyViewModel.onEvent(event)
            }
        }
    )
}

@Composable
fun EmergencyScreenContent(
    state: EmergencyScreenState,
    onEvent: (EmergencyScreenUserEvent) -> Unit
) {
    Scaffold { paddingValues ->
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
                .padding(paddingValues = paddingValues)
        ) {
            IconButton(
                onClick = {
                    onEvent(EmergencyScreenUserEvent.OnCloseClicked)
                },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(all = MaterialTheme.space.smallMedium)
            ) {
                Icon(
                    imageVector = QRAlarmIcons.Close,
                    contentDescription = stringResource(R.string.content_description_close_icon),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(all = MaterialTheme.space.medium),
                visible = state.alarmMuteProgress != null
            ) {
                AlarmMuteIndicator(
                    progress = state.alarmMuteProgress ?: 0f,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            AnimatedContent(
                targetState = state.isTaskStarted,
                modifier = Modifier.align(alignment = Alignment.Center)
            ) { isTaskStarted ->
                if (!isTaskStarted) {
                    EmergencyInfoCard(
                        onStartClick = {
                            onEvent(EmergencyScreenUserEvent.OnTaskStarted)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = MaterialTheme.space.medium)
                    )
                } else {
                    EmergencyTaskCard(
                        emergencyTaskConfig = state.emergencyTaskConfig,
                        onValueChanged = {
                            onEvent(EmergencyScreenUserEvent.OnTaskValueChanged(it))
                        },
                        onValueSelected = {
                            onEvent(EmergencyScreenUserEvent.OnTaskValueSelected)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = MaterialTheme.space.medium)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EmergencyScreenContentPreview() {
    QRAlarmTheme {
        EmergencyScreenContent(
            state = EmergencyScreenState(),
            onEvent = {}
        )
    }
}
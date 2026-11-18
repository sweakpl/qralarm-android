package com.sweak.qralarm.features.emergency.task

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.service.AlarmService.Companion.ACTION_EMERGENCY_HOLD_MUTE
import com.sweak.qralarm.alarm.service.AlarmService.Companion.ACTION_EMERGENCY_RELEASE_MUTE
import com.sweak.qralarm.core.domain.user.model.EmergencyTaskType
import com.sweak.qralarm.features.emergency.task.components.RingAgainDialog
import com.sweak.qralarm.features.emergency.task.components.WrittenExplanationCard
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.widget.Toast
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.features.emergency.task.components.AlarmMuteIndicator
import com.sweak.qralarm.features.emergency.task.components.EmergencyInfoCard
import com.sweak.qralarm.features.emergency.task.components.EmergencyTaskCard

@Composable
fun EmergencyScreen(
    idOfAlarmToCancel: Long,
    onCloseClicked: () -> Unit,
    onEmergencyTaskCompleted: () -> Unit
) {
    val emergencyViewModel =
        hiltViewModel<EmergencyViewModel, EmergencyViewModel.Factory> { factory ->
            factory.create(idOfAlarmToCancel = idOfAlarmToCancel)
        }
    val emergencyScreenState by emergencyViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler { /* no-op */ }

    fun broadcast(action: String) =
        context.sendBroadcast(Intent(action).apply { setPackage(context.packageName) })

    ObserveAsEvents(
        flow = emergencyViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is EmergencyScreenBackendEvent.HoldAlarmSilent -> broadcast(ACTION_EMERGENCY_HOLD_MUTE)
                is EmergencyScreenBackendEvent.ReleaseAlarm -> broadcast(ACTION_EMERGENCY_RELEASE_MUTE)
                is EmergencyScreenBackendEvent.RingAgainScheduled ->
                    Toast.makeText(context, R.string.ring_again_scheduled, Toast.LENGTH_SHORT).show()
                is EmergencyScreenBackendEvent.EmergencyTaskCompleted -> onEmergencyTaskCompleted()
            }
        }
    )

    // The alarm stays silent only while this screen is actually in front of the user.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> emergencyViewModel.onEvent(
                    EmergencyScreenUserEvent.OnScreenVisibilityChanged(isVisible = true)
                )
                Lifecycle.Event.ON_PAUSE -> emergencyViewModel.onEvent(
                    EmergencyScreenUserEvent.OnScreenVisibilityChanged(isVisible = false)
                )
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            emergencyViewModel.onEvent(EmergencyScreenUserEvent.OnScreenVisibilityChanged(isVisible = false))
            // Leaving without finishing must not leave the alarm silent until the watchdog runs out.
            if (!emergencyViewModel.state.value.let { it.emergencyTaskConfig.isCompleted || it.explanation.isCompleted }) {
                broadcast(ACTION_EMERGENCY_RELEASE_MUTE)
            }
        }
    }

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
                .then(
                    if (MaterialTheme.isQRAlarmTheme)
                        Modifier.background(
                            brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac))
                        )
                    else Modifier
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
                    contentDescription = stringResource(R.string.content_description_close_icon)
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(all = MaterialTheme.space.medium),
                visible = state.isAlarmHeldSilent
            ) {
                AlarmMuteIndicator(
                    progress = state.idleSecondsRemaining?.let { it / 20f } ?: 1f
                )
            }

            state.idleSecondsRemaining?.let { seconds ->
                androidx.compose.material3.Text(
                    text = stringResource(R.string.emergency_idle_warning, seconds),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = MaterialTheme.space.xLarge, start = 64.dp, end = 64.dp)
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
                } else if (state.taskType == EmergencyTaskType.WRITTEN_EXPLANATION) {
                    WrittenExplanationCard(
                        explanation = state.explanation,
                        onTextChanged = { onEvent(EmergencyScreenUserEvent.OnExplanationChanged(it)) },
                        onSubmit = { onEvent(EmergencyScreenUserEvent.OnSubmitExplanation) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
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

    if (state.showRingAgainChoice) {
        RingAgainDialog(
            onRingInMinutes = { onEvent(EmergencyScreenUserEvent.OnRingAgainInMinutes(it)) },
            onRingAt = { hour, minute -> onEvent(EmergencyScreenUserEvent.OnRingAgainAt(hour, minute)) },
            onDecline = { onEvent(EmergencyScreenUserEvent.OnRingAgainDeclined) }
        )
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
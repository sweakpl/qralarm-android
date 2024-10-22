package com.sweak.qralarm.features.alarm

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.core.ui.getTimeString

@Composable
fun AlarmScreen(
    onStopAlarm: () -> Unit,
    onRequestCodeScan: () -> Unit
) {
    val alarmViewModel = hiltViewModel<AlarmViewModel>()

    ObserveAsEvents(
        flow = alarmViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is AlarmScreenBackendEvent.StopAlarm -> onStopAlarm()
                is AlarmScreenBackendEvent.RequestCodeScanToStopAlarm -> onRequestCodeScan()
            }
        }
    )

    AlarmScreenContent(
        state = AlarmScreenState(),
        onEvent = { event ->
            when (event) {
                AlarmScreenUserEvent.StopAlarmClicked -> alarmViewModel.onEvent(event)
            }
        }
    )
}

@Composable
private fun AlarmScreenContent(
    state: AlarmScreenState,
    onEvent: (AlarmScreenUserEvent) -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
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
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.xLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.alarm_wake_up),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = getTimeString(
                        timeInMillis = state.currentTimeInMillis,
                        is24HourFormat = DateFormat.is24HourFormat(LocalContext.current)
                    ),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1
                )
            }

            Column {
                Button(
                    onClick = { onEvent(AlarmScreenUserEvent.StopAlarmClicked) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.stop),
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(all = MaterialTheme.space.small)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlarmScreenContentPreview() {
    QRAlarmTheme {
        AlarmScreenContent(
            state= AlarmScreenState(),
            onEvent = {}
        )
    }
}
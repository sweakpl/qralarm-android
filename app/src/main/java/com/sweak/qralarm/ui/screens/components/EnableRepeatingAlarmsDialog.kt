package com.sweak.qralarm.ui.screens.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.home.HomeUiState

@Composable
fun EnableRepeatingAlarmsDialog(
    uiState: MutableState<HomeUiState>,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    if (uiState.value.showEnableRepeatingAlarmsDialog) {
        Dialog(
            onDismissRequest = {
                uiState.value = uiState.value.copy(showAlarmPermissionDialog = false)
            },
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            title = stringResource(R.string.enable_repeating_alarms_title),
            message = stringResource(R.string.enable_repeating_alarms_message),
            positiveButtonText = stringResource(R.string.enable),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }
}
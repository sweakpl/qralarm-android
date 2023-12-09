package com.sweak.qralarm.ui.screens.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.scanner.ScannerUiState

@Composable
fun DismissCodeAddedDialog(
    uiState: MutableState<ScannerUiState>,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    if (uiState.value.showDismissCodeAddedDialog && !uiState.value.hasNewDismissCodeBeenAccepted) {
        Dialog(
            onDismissRequest = {
                uiState.value = uiState.value.copy(showDismissCodeAddedDialog = false)
            },
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            title = stringResource(R.string.new_code_added_title),
            message = stringResource(
                R.string.new_code_added_message,
                uiState.value.newDismissAlarmCode ?: "CODE_READ_ERROR"
            ),
            positiveButtonText = stringResource(R.string.it_is_ok),
            negativeButtonText = stringResource(R.string.again)
        )
    }
}
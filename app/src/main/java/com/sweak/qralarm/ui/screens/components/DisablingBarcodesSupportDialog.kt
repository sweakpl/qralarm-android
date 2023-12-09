package com.sweak.qralarm.ui.screens.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.settings.SettingsUiState

@Composable
fun DisablingBarcodesSupportDialog(
    uiState: MutableState<SettingsUiState>,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    if (uiState.value.showDisablingBarcodesSupportDialog) {
        Dialog(
            onDismissRequest = {
                uiState.value = uiState.value.copy(showDisablingBarcodesSupportDialog = false)
            },
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            title = stringResource(R.string.disabling_barcodes_title),
            message = stringResource(R.string.disabling_barcodes_message),
            positiveButtonText = stringResource(R.string.yes),
            negativeButtonText = stringResource(R.string.no)
        )
    }
}
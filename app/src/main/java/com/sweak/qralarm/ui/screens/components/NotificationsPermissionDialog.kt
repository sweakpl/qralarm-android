package com.sweak.qralarm.ui.screens.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.home.HomeUiState

@Composable
fun NotificationsPermissionDialog(
    uiState: MutableState<HomeUiState>,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    if (uiState.value.showNotificationsPermissionDialog) {
        Dialog(
            onDismissRequest = {
                uiState.value = uiState.value.copy(showNotificationsPermissionDialog = false)
            },
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            title = stringResource(R.string.notifications_permission_required_title),
            message = stringResource(R.string.notifications_permission_required_message),
            positiveButtonText = stringResource(R.string.allow),
            negativeButtonText = stringResource(R.string.later)
        )
    }
}
package com.sweak.qralarm.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingPermissionsBottomSheet(
    cameraPermissionState: Boolean? = null,
    onCameraPermissionClick: (() -> Unit)? = null,
    alarmsPermissionState: Boolean? = null,
    onAlarmsPermissionClick: (() -> Unit)? = null,
    notificationsPermissionState: Boolean? = null,
    onNotificationsPermissionClick: (() -> Unit)? = null,
    fullScreenIntentPermissionState: Boolean? = null,
    onFullScreenIntentPermissionClick: (() -> Unit)? = null,
    onAllPermissionsGranted: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(
        cameraPermissionState,
        alarmsPermissionState,
        notificationsPermissionState,
        fullScreenIntentPermissionState
    ) {
        if ((cameraPermissionState == null || cameraPermissionState) &&
            (alarmsPermissionState == null || alarmsPermissionState) &&
            (notificationsPermissionState == null || notificationsPermissionState) &&
            (fullScreenIntentPermissionState == null || fullScreenIntentPermissionState)
        ) {
            launch {
                modalBottomSheetState.hide()
            }.invokeOnCompletion {
                onAllPermissionsGranted()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.space.mediumLarge,
                    end = MaterialTheme.space.mediumLarge,
                    bottom = MaterialTheme.space.xLarge
                )
        ) {
            Text(
                text = stringResource(R.string.permissions_required),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
            )

            Separator()

            cameraPermissionState?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !cameraPermissionState) {
                            onCameraPermissionClick?.invoke()
                        }
                ) {
                    Icon(
                        imageVector = QRAlarmIcons.Camera,
                        contentDescription = stringResource(
                            R.string.content_description_camera_icon
                        )
                    )

                    Column(
                        modifier = Modifier
                            .padding(all = MaterialTheme.space.medium)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.camera),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                        )

                        Text(
                            text = stringResource(R.string.camera_usage),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Icon(
                        imageVector =
                        if (cameraPermissionState) QRAlarmIcons.Done else QRAlarmIcons.ForwardArrow,
                        contentDescription = stringResource(
                            if (cameraPermissionState) R.string.content_description_done_icon
                            else R.string.content_description_forward_arrow_icon
                        )
                    )
                }

                Separator()
            }

            alarmsPermissionState?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !alarmsPermissionState) {
                            onAlarmsPermissionClick?.invoke()
                        }
                ) {
                    Icon(
                        imageVector = QRAlarmIcons.Alarm,
                        contentDescription = stringResource(R.string.content_description_alarm_icon)
                    )

                    Column(
                        modifier = Modifier
                            .padding(all = MaterialTheme.space.medium)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.alarms),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                        )

                        Text(
                            text = stringResource(R.string.alarms_usage),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Icon(
                        imageVector =
                        if (alarmsPermissionState) QRAlarmIcons.Done else QRAlarmIcons.ForwardArrow,
                        contentDescription = stringResource(
                            if (alarmsPermissionState) R.string.content_description_done_icon
                            else R.string.content_description_forward_arrow_icon
                        )
                    )
                }

                Separator()
            }

            notificationsPermissionState?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !notificationsPermissionState) {
                            onNotificationsPermissionClick?.invoke()
                        }
                ) {
                    Icon(
                        imageVector = QRAlarmIcons.Notification,
                        contentDescription =
                        stringResource(R.string.content_description_notification_icon)
                    )

                    Column(
                        modifier = Modifier
                            .padding(all = MaterialTheme.space.medium)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.notifications),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                        )

                        Text(
                            text = stringResource(R.string.notifications_usage),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Icon(
                        imageVector =
                        if (notificationsPermissionState) QRAlarmIcons.Done
                        else QRAlarmIcons.ForwardArrow,
                        contentDescription = stringResource(
                            if (notificationsPermissionState) R.string.content_description_done_icon
                            else R.string.content_description_forward_arrow_icon
                        )
                    )
                }

                Separator()
            }

            fullScreenIntentPermissionState?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !fullScreenIntentPermissionState) {
                            onFullScreenIntentPermissionClick?.invoke()
                        }
                ) {
                    Icon(
                        imageVector = QRAlarmIcons.FullScreen,
                        contentDescription =
                        stringResource(R.string.content_description_full_screen_icon)
                    )

                    Column(
                        modifier = Modifier
                            .padding(all = MaterialTheme.space.medium)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.full_screen_display),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                        )

                        Text(
                            text = stringResource(R.string.full_screen_display_usage),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Icon(
                        imageVector =
                        if (fullScreenIntentPermissionState) QRAlarmIcons.Done
                        else QRAlarmIcons.ForwardArrow,
                        contentDescription = stringResource(
                            if (fullScreenIntentPermissionState) R.string.content_description_done_icon
                            else R.string.content_description_forward_arrow_icon
                        )
                    )
                }

                Separator()
            }
        }
    }
}

@Composable
private fun Separator() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color = MaterialTheme.colorScheme.onSurface)
    )
}

@Preview
@Composable
private fun MissingPermissionsBottomSheetPreview() {
    QRAlarmTheme {
        MissingPermissionsBottomSheet(
            cameraPermissionState = false,
            onCameraPermissionClick = {},
            alarmsPermissionState = true,
            onAlarmsPermissionClick = {},
            notificationsPermissionState = false,
            onNotificationsPermissionClick = {},
            fullScreenIntentPermissionState = false,
            onFullScreenIntentPermissionClick = {},
            onAllPermissionsGranted = {},
            onDismissRequest = {}
        )
    }
}
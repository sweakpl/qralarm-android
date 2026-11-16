package com.sweak.qralarm.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import com.sweak.qralarm.core.ui.model.AlarmPermission
import com.sweak.qralarm.core.ui.model.PermissionsDialogState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingPermissionsBottomSheet(
    permissionsDialogState: PermissionsDialogState,
    onPermissionClick: (AlarmPermission) -> Unit,
    onAllPermissionsGranted: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(permissionsDialogState.areAllPermissionsGranted) {
        if (permissionsDialogState.areAllPermissionsGranted) {
            launch {
                modalBottomSheetState.hide()
            }.invokeOnCompletion { cause ->
                if (cause == null) onAllPermissionsGranted()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = modalBottomSheetState
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

            HorizontalDivider(
                thickness = 1.dp,
                color = LocalContentColor.current
            )

            permissionsDialogState.cameraPermissionState?.let { isGranted ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !isGranted) {
                            onPermissionClick(AlarmPermission.CAMERA)
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
                            if (isGranted) QRAlarmIcons.Done else QRAlarmIcons.ForwardArrow,
                        contentDescription = stringResource(
                            if (isGranted) R.string.content_description_done_icon
                            else R.string.content_description_forward_arrow_icon
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = LocalContentColor.current
                )
            }

            permissionsDialogState.alarmsPermissionState?.let { isGranted ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !isGranted) {
                            onPermissionClick(AlarmPermission.EXACT_ALARMS)
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
                            if (isGranted) QRAlarmIcons.Done else QRAlarmIcons.ForwardArrow,
                        contentDescription = stringResource(
                            if (isGranted) R.string.content_description_done_icon
                            else R.string.content_description_forward_arrow_icon
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = LocalContentColor.current
                )
            }

            permissionsDialogState.notificationsPermissionState?.let { isGranted ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !isGranted) {
                            onPermissionClick(AlarmPermission.NOTIFICATIONS)
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
                            if (isGranted) QRAlarmIcons.Done
                            else QRAlarmIcons.ForwardArrow,
                        contentDescription = stringResource(
                            if (isGranted) R.string.content_description_done_icon
                            else R.string.content_description_forward_arrow_icon
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = LocalContentColor.current
                )
            }

            permissionsDialogState.fullScreenIntentPermissionState?.let { isGranted ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(enabled = !isGranted) {
                            onPermissionClick(AlarmPermission.FULL_SCREEN_INTENT)
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
                            if (isGranted) QRAlarmIcons.Done
                            else QRAlarmIcons.ForwardArrow,
                        contentDescription = stringResource(
                            if (isGranted) R.string.content_description_done_icon
                            else R.string.content_description_forward_arrow_icon
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = LocalContentColor.current
                )
            }
        }
    }
}

@Preview
@Composable
private fun MissingPermissionsBottomSheetPreview() {
    QRAlarmTheme {
        MissingPermissionsBottomSheet(
            permissionsDialogState = PermissionsDialogState(
                isVisible = true,
                cameraPermissionState = false,
                notificationsPermissionState = false,
                alarmsPermissionState = true,
                fullScreenIntentPermissionState = false
            ),
            onPermissionClick = {},
            onAllPermissionsGranted = {},
            onDismissRequest = {}
        )
    }
}
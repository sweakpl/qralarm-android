package com.sweak.qralarm.core.ui.compose_util

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sweak.qralarm.R
import com.sweak.qralarm.core.ui.model.AlarmPermission

@OptIn(ExperimentalPermissionsApi::class)
@Stable
class AlarmPermissionsRequester internal constructor(
    private val context: Context,
    private val cameraPermissionState: PermissionState,
    private val notificationsPermissionState: PermissionState?,
    private val onCameraRationaleRequired: State<() -> Unit>,
    private val onNotificationsRationaleRequired: State<(() -> Unit)?>
) {
    val isCameraPermissionGranted: Boolean
        get() = cameraPermissionState.status.isGranted

    val isNotificationsPermissionGranted: Boolean
        get() = notificationsPermissionState?.status?.isGranted ?: true

    fun request(permission: AlarmPermission) {
        when (permission) {
            AlarmPermission.CAMERA -> requestRuntimePermission(
                permissionState = cameraPermissionState,
                onRationaleRequired = onCameraRationaleRequired.value
            )

            AlarmPermission.NOTIFICATIONS -> notificationsPermissionState?.let {
                requestRuntimePermission(
                    permissionState = it,
                    onRationaleRequired = onNotificationsRationaleRequired.value ?: {}
                )
            }

            AlarmPermission.EXACT_ALARMS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    openPackageSettings(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                }
            }

            AlarmPermission.FULL_SCREEN_INTENT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    openPackageSettings(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                }
            }
        }
    }

    fun openApplicationSettings() {
        openPackageSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    }

    private fun requestRuntimePermission(
        permissionState: PermissionState,
        onRationaleRequired: () -> Unit
    ) {
        if (permissionState.status.shouldShowRationale) {
            onRationaleRequired()
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    private fun openPackageSettings(action: String) {
        try {
            context.startActivity(
                Intent(action).apply {
                    data = "package:${context.packageName}".toUri()
                }
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.issue_opening_the_page, Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberAlarmPermissionsRequester(
    onCameraRationaleRequired: () -> Unit,
    onNotificationsRationaleRequired: (() -> Unit)? = null
): AlarmPermissionsRequester {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val notificationsPermissionState =
        if (onNotificationsRationaleRequired != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            null
        }
    val currentOnCameraRationaleRequired = rememberUpdatedState(onCameraRationaleRequired)
    val currentOnNotificationsRationaleRequired =
        rememberUpdatedState(onNotificationsRationaleRequired)

    return remember(context, cameraPermissionState, notificationsPermissionState) {
        AlarmPermissionsRequester(
            context = context,
            cameraPermissionState = cameraPermissionState,
            notificationsPermissionState = notificationsPermissionState,
            onCameraRationaleRequired = currentOnCameraRationaleRequired,
            onNotificationsRationaleRequired = currentOnNotificationsRationaleRequired
        )
    }
}

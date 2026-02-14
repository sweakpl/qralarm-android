package com.sweak.qralarm.core.ui.components.code_scanner.compose

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.custom_code_scanner.components.ScanOverlay

@Composable
fun CodeScanner(
    surfaceRequest: SurfaceRequest?,
    isFlashEnabled: Boolean,
    onCloseClicked: () -> Unit,
    onToggleFlash: () -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                implementationMode = ImplementationMode.EMBEDDED,
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize()
            )
        }

        ScanOverlay()

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) +
                            MaterialTheme.space.mediumLarge,
                    top = paddingValues.calculateTopPadding() + MaterialTheme.space.mediumLarge,
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) +
                            MaterialTheme.space.mediumLarge
                )
        ) {
            IconButton(onClick = onCloseClicked) {
                Icon(
                    imageVector = QRAlarmIcons.Close,
                    contentDescription = stringResource(R.string.content_description_close_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MaterialTheme.space.xLarge)
                )
            }

            IconButton(onClick = onToggleFlash) {
                Icon(
                    imageVector =
                        if (isFlashEnabled) QRAlarmIcons.FlashOff
                        else QRAlarmIcons.FlashOn,
                    contentDescription = stringResource(R.string.content_description_flash_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MaterialTheme.space.xLarge)
                )
            }
        }
    }
}
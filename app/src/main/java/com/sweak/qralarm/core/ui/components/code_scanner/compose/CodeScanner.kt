package com.sweak.qralarm.core.ui.components.code_scanner.compose

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.components.CodeNameChip
import com.sweak.qralarm.core.ui.components.code_scanner.ScanOverlay
import com.sweak.qralarm.core.ui.components.object_scanner.ObjectDetectionBox
import kotlin.math.min

@Composable
fun CodeScanner(
    surfaceRequest: SurfaceRequest?,
    isFlashEnabled: Boolean,
    codeName: String?,
    onCloseClicked: () -> Unit,
    onToggleFlash: () -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    objectName: String? = null,
    detectionBox: ObjectDetectionBox? = null,
    confirmationProgress: Float = 0f,
    recognitionError: Boolean = false,
    isCustomObject: Boolean = false
) {
    Box(modifier = modifier) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                implementationMode = ImplementationMode.EMBEDDED,
                surfaceRequest = request,
                contentScale = if (objectName == null) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (objectName == null) ScanOverlay()
        else {
            detectionBox?.let { box ->
                Canvas(Modifier.fillMaxSize()) {
                    val scale = min(size.width / box.imageWidth, size.height / box.imageHeight)
                    val width = box.imageWidth * scale
                    val height = box.imageHeight * scale
                    val left = (size.width - width) / 2 + box.left * width
                    val top = (size.height - height) / 2 + box.top * height
                    drawRect(
                        Color.Green, Offset(left, top),
                        Size((box.right - box.left) * width, (box.bottom - box.top) * height),
                        style = Stroke(3.dp.toPx())
                    )
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 24.dp, start = 16.dp, end = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(
                        if (isCustomObject) R.string.recognition_target_custom else R.string.recognition_target,
                        objectName
                    ))
                    if (recognitionError) Text(stringResource(R.string.recognition_error), color = MaterialTheme.colorScheme.error)
                    else {
                        if (confirmationProgress > 0f) Text(stringResource(R.string.recognition_progress))
                        LinearProgressIndicator(
                            progress = { confirmationProgress },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp)
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onCloseClicked) {
                    Icon(
                        imageVector = QRAlarmIcons.Close,
                        contentDescription =
                            stringResource(R.string.content_description_close_icon),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(MaterialTheme.space.xLarge)
                    )
                }

                IconButton(onClick = onToggleFlash) {
                    Icon(
                        imageVector =
                            if (isFlashEnabled) QRAlarmIcons.FlashOff
                            else QRAlarmIcons.FlashOn,
                        contentDescription =
                            stringResource(R.string.content_description_flash_icon),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(MaterialTheme.space.xLarge)
                    )
                }
            }

            AnimatedVisibility(
                visible = codeName != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.padding(top = MaterialTheme.space.medium)
            ) {
                CodeNameChip(codeName = codeName ?: "")
            }
        }
    }
}

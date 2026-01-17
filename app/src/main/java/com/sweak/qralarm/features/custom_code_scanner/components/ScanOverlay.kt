package com.sweak.qralarm.features.custom_code_scanner.components

import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

@Composable
fun ScanOverlay() {
    val ratio = SCAN_OVERLAY_RATIO
    val overlayColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val cornerColor = MaterialTheme.colorScheme.primary
    val cornerStrokeDp = 2f
    val cornerLengthDp = 48f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val overlayWidth = size.width
        val overlayHeight = size.height
        val viewfinderSize = minOf(overlayWidth, overlayHeight) * ratio

        val left = (overlayWidth - viewfinderSize) / 2f
        val top = (overlayHeight - viewfinderSize) / 2f
        val right = left + viewfinderSize
        val bottom = top + viewfinderSize
        val rect = Rect(left, top, right, bottom)

        // Draw dimmed background and cut out the viewfinder using a saved layer + CLEAR xfermode
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas

            val overlayPaint = android.graphics.Paint().apply {
                color = overlayColor.toArgb()
                style = android.graphics.Paint.Style.FILL
            }
            val clearPaint = android.graphics.Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }

            val saveCount = native.saveLayer(null, null)
            // draw full-screen dim overlay on the layer
            native.drawRect(0f, 0f, size.width, size.height, overlayPaint)
            // cut out transparent rectangle
            native.drawRect(left, top, right, bottom, clearPaint)
            native.restoreToCount(saveCount)
        }

        // Draw corners
        val cornerLength = cornerLengthDp * density
        val stroke = cornerStrokeDp * density

        // Top-left
        drawLine(
            color = cornerColor,
            start = rect.topLeft,
            end = rect.topLeft.copy(x = rect.left + cornerLength),
            strokeWidth = stroke
        )
        drawLine(
            color = cornerColor,
            start = rect.topLeft,
            end = rect.topLeft.copy(y = rect.top + cornerLength),
            strokeWidth = stroke
        )
        // Top-right
        drawLine(
            color = cornerColor,
            start = rect.topRight,
            end = rect.topRight.copy(x = rect.right - cornerLength),
            strokeWidth = stroke
        )
        drawLine(
            color = cornerColor,
            start = rect.topRight,
            end = rect.topRight.copy(y = rect.top + cornerLength),
            strokeWidth = stroke
        )
        // Bottom-right
        drawLine(
            color = cornerColor,
            start = rect.bottomRight,
            end = rect.bottomRight.copy(x = rect.right - cornerLength),
            strokeWidth = stroke
        )
        drawLine(
            color = cornerColor,
            start = rect.bottomRight,
            end = rect.bottomRight.copy(y = rect.bottom - cornerLength),
            strokeWidth = stroke
        )
        // Bottom-left
        drawLine(
            color = cornerColor,
            start = rect.bottomLeft,
            end = rect.bottomLeft.copy(x = rect.left + cornerLength),
            strokeWidth = stroke
        )
        drawLine(
            color = cornerColor,
            start = rect.bottomLeft,
            end = rect.bottomLeft.copy(y = rect.bottom - cornerLength),
            strokeWidth = stroke
        )
    }
}

const val SCAN_OVERLAY_RATIO = 0.7f
package com.sweak.qralarm.core.ui.components.code_scanner.analyzer

import androidx.camera.core.ImageProxy
import com.sweak.qralarm.core.ui.components.code_scanner.view.ScanOverlay
import kotlin.math.roundToInt

class CodeAnalyzer(
    barcodeDetector: BarcodeDetector
) : AbstractCodeAnalyzer(barcodeDetector) {

    override fun analyze(image: ImageProxy) {
        val plane = image.planes[0]
        val imageData = plane.buffer.toByteArray()

        val size = image.width.coerceAtMost(image.height) * ScanOverlay.RATIO

        val left = (image.width - size) / 2f
        val top = (image.height - size) / 2f

        analyse(
            yuvData = imageData,
            dataWidth = plane.rowStride,
            dataHeight = image.height,
            left = left.roundToInt(),
            top = top.roundToInt(),
            width = size.roundToInt(),
            height = size.roundToInt()
        )

        image.close()
    }
}
package com.sweak.qralarm.core.ui.components.code_scanner.analyzer

import androidx.camera.core.ImageProxy
import com.sweak.qralarm.core.ui.components.code_scanner.view.ScanOverlay
import kotlin.math.roundToInt

class CodeAnalyzer(
    barcodeDetector: BarcodeDetector
) : AbstractCodeAnalyzer(barcodeDetector) {

    override fun analyze(image: ImageProxy) {
        image.use { img ->
            val plane = img.planes[0]
            val imageData = plane.buffer.toByteArray()

            val size = img.width.coerceAtMost(img.height) * ScanOverlay.RATIO

            val left = (img.width - size) / 2f
            val top = (img.height - size) / 2f

            analyse(
                yuvData = imageData,
                dataWidth = plane.rowStride,
                dataHeight = img.height,
                left = left.roundToInt(),
                top = top.roundToInt(),
                width = size.roundToInt(),
                height = size.roundToInt()
            )
        }
    }
}
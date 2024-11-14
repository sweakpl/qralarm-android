package com.sweak.qralarm.core.ui.components.code_scanner.analyzer

import androidx.camera.core.ImageProxy
import com.sweak.qralarm.core.ui.components.code_scanner.view.ScanOverlay
import kotlin.math.roundToInt

class LegacyCodeAnalyzer(
    barcodeDetector: BarcodeDetector
) : AbstractCodeAnalyzer(barcodeDetector) {

    override fun analyze(image: ImageProxy) {
        val plane = image.planes[0]
        val rotationDegrees = image.imageInfo.rotationDegrees

        val byteArray: ByteArray
        val imageWidth: Int
        val imageHeight: Int

        if (rotationDegrees == 0 || rotationDegrees == 180) {
            byteArray = plane.buffer.toByteArray()
            imageWidth = image.width
            imageHeight = image.height
        } else {
            byteArray = rotateImageArray(plane.buffer.toByteArray(), image.width, image.height, rotationDegrees)
            imageWidth = image.height
            imageHeight = image.width
        }

        val size = imageWidth.coerceAtMost(imageHeight) * ScanOverlay.RATIO

        val left = (imageWidth - size) / 2f
        val top = (imageHeight - size) / 2f

        analyse(
            yuvData = byteArray,
            dataWidth = imageWidth,
            dataHeight = imageHeight,
            left = left.roundToInt(),
            top = top.roundToInt(),
            width = size.roundToInt(),
            height = size.roundToInt()
        )

        image.close()
    }

    // 90, 180. 270 rotation
    private fun rotateImageArray(byteArray: ByteArray, width: Int, height: Int, rotationDegrees: Int): ByteArray {
        if (rotationDegrees == 0) return byteArray
        if (rotationDegrees % 90 != 0) return byteArray

        val rotatedByteArray = ByteArray(byteArray.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                when (rotationDegrees) {
                    90 -> rotatedByteArray[x * height + height - y - 1] = byteArray[x + y * width]
                    180 -> rotatedByteArray[width * (height - y - 1) + width - x - 1] = byteArray[x + y * width]
                    270 -> rotatedByteArray[y + x * height] = byteArray[y * width + width - x - 1]
                }
            }
        }

        return rotatedByteArray
    }

    /*private data class Values(
        val byteArray: ByteArray,
        val imageWidth: Int,
        val imageHeight: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Values

            if (!byteArray.contentEquals(other.byteArray)) return false
            if (imageWidth != other.imageWidth) return false
            if (imageHeight != other.imageHeight) return false

            return true
        }

        /*override fun hashCode(): Int {
            var result = byteArray.contentHashCode()
            result = 31 * result + imageWidth
            result = 31 * result + imageHeight
            return result
        }*/
    }*/
}
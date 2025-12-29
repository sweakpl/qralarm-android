package com.sweak.qralarm.core.ui.components.code_scanner.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.sweak.qralarm.features.custom_code_scanner.components.SCAN_OVERLAY_RATIO
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class ZXingCodeAnalyzer(
    private val codeDetector: CodeDetector
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val map = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.entries
        )
        setHints(map)
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    override fun analyze(image: ImageProxy) {
        if (image.planes.isEmpty()) {
            image.close()
            return
        }

        image.use { img ->
            val plane = img.planes[0]
            val imageData = plane.buffer.toByteArray()

            val size = img.width.coerceAtMost(img.height) * SCAN_OVERLAY_RATIO

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

    fun analyse(
        yuvData: ByteArray,
        dataWidth: Int,
        dataHeight: Int,
        left: Int,
        top: Int,
        width: Int,
        height: Int
    ) {
        try {
            val source = PlanarYUVLuminanceSource(
                yuvData,
                dataWidth,
                dataHeight,
                left,
                top,
                width,
                height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            reader.reset()
            try {
                val result = reader.decode(binaryBitmap)
                codeDetector.onCodeFound(result.text)
            } catch (_: ReaderException) {
                val invertedSource = source.invert()
                val invertedBinaryBitmap = BinaryBitmap(HybridBinarizer(invertedSource))
                reader.reset()
                try {
                    val result = reader.decode(invertedBinaryBitmap)
                    codeDetector.onCodeFound(result.text)
                } catch (_: ReaderException) {
                    // Not Found
                }
            }
        } catch (e: Exception) {
            codeDetector.onError(e)
        }
    }
}
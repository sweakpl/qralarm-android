package com.sweak.qralarm.util

import android.graphics.ImageFormat
import android.os.Build
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QRCodeAnalyzer(
    private val onQRCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val supportedImageFormats = mutableListOf(
        ImageFormat.YUV_420_888,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            add(ImageFormat.YUV_422_888)
            add(ImageFormat.YUV_444_888)
        }
    }.toList()

    override fun analyze(image: ImageProxy) {
        if (image.format in supportedImageFormats) {
            val imageBytes = image.planes.first().buffer.toByteArray()

            val luminanceSource = PlanarYUVLuminanceSource(
                imageBytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )

            val binaryImageBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))

            try {
                val decodingResult = MultiFormatReader().apply {
                    setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.QR_CODE))
                }.decode(binaryImageBitmap)
                onQRCodeScanned(decodingResult.text)
            } catch (exception: NotFoundException) {
                exception.printStackTrace()
            } finally {
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }
}
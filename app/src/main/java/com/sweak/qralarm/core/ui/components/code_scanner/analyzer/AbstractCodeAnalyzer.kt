package com.sweak.qralarm.core.ui.components.code_scanner.analyzer

import androidx.camera.core.ImageAnalysis
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer

abstract class AbstractCodeAnalyzer(
    private val barcodeDetector: BarcodeDetector
) : ImageAnalysis.Analyzer {

    interface BarcodeDetector {
        fun onBarcodeFound(result: Result)
        fun onError(msg: String)
    }

    private val reader = MultiFormatReader().apply {
        val map = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.entries
        )
        setHints(map)
    }

    protected fun analyse(
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
                barcodeDetector.onBarcodeFound(result)
            } catch (e: ReaderException) {
                val invertedSource = source.invert()
                val invertedBinaryBitmap = BinaryBitmap(HybridBinarizer(invertedSource))
                reader.reset()
                try {
                    val result = reader.decode(invertedBinaryBitmap)
                    barcodeDetector.onBarcodeFound(result)
                } catch (e: ReaderException) {
                    //e.printStackTrace() // Not Found
                }
            }
        } catch (e: Exception) {
            barcodeDetector.onError(e.toString())
        }
    }
}
package com.sweak.qralarm.core.ui.components.code_scanner.analyzer

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class CodeAnalyzer(
    private val codeDetector: CodeDetector
) : ImageAnalysis.Analyzer {

    interface CodeDetector {
        fun onCodeFound(codeValue: String)
        fun onError(exception: Exception)
    }

    private val barcodeScanner by lazy {
        val optionsBuilder = BarcodeScannerOptions.Builder().enableAllPotentialBarcodes()

        try {
            BarcodeScanning.getClient(optionsBuilder.build())
        } catch (e: Exception) {
            codeDetector.onError(e)
            null
        }
    }

    @Volatile
    private var failureOccurred = false
    private var failureTimestamp = 0L

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image == null) return

        if (failureOccurred && System.currentTimeMillis() - failureTimestamp < 1000L) {
            imageProxy.close()
            return
        }

        failureOccurred = false

        barcodeScanner?.let { scanner ->
            scanner.process(
                InputImage.fromMediaImage(
                    imageProxy.image!!,
                    imageProxy.imageInfo.rotationDegrees
                )
            )
                .addOnSuccessListener { codes ->
                    codes.firstNotNullOfOrNull { it }?.let {
                        it.rawValue?.let { codeValue ->
                            if (codeValue.isNotEmpty()) {
                                codeDetector.onCodeFound(codeValue)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    failureOccurred = true
                    failureTimestamp = System.currentTimeMillis()

                    codeDetector.onError(it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
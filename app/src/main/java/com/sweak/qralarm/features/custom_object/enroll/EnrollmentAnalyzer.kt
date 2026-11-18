package com.sweak.qralarm.features.custom_object.enroll

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.sweak.qralarm.core.domain.custom_object.CustomObjectReference
import com.sweak.qralarm.core.recognition.custom.EnrollmentRules
import com.sweak.qralarm.core.recognition.custom.OrbExtractor
import com.sweak.qralarm.core.recognition.custom.ReferenceBuilder
import com.sweak.qralarm.core.recognition.custom.ReferenceFeatures
import com.sweak.qralarm.core.ui.components.object_scanner.CustomObjectAnalyzer
import com.sweak.qralarm.core.ui.components.object_scanner.CustomObjectTarget
import com.sweak.qralarm.core.ui.components.object_scanner.ImageEmbedderEngine
import com.sweak.qralarm.core.ui.components.object_scanner.ObjectDetectionBox
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/** Captures reference shots on request, and later runs the real recognizer as a test. */
class EnrollmentAnalyzer(
    private val context: Context,
    private val onShot: (Shot) -> Unit,
    private val onTestResult: (ObjectDetectionBox?, Float, Boolean) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer, AutoCloseable {

    class Shot(val reference: CustomObjectReference, val issue: EnrollmentRules.ShotIssue?, val preview: Bitmap)

    private val captureRequested = AtomicBoolean(false)
    private class Test(val references: List<ReferenceFeatures>, val embeddingThreshold: Float)
    @Volatile private var test: Test? = null
    private var tester: CustomObjectAnalyzer? = null
    private var extractor: OrbExtractor? = null
    private var embedder: ImageEmbedderEngine? = null

    fun requestCapture() = captureRequested.set(true)

    /** Runs exactly what the alarm will run; null references stop testing. */
    fun startTest(references: List<ReferenceFeatures>?, embeddingThreshold: Float = 0f) {
        test = references?.let { Test(it, embeddingThreshold) }
    }

    override fun analyze(image: ImageProxy) {
        val currentTest = test
        if (currentTest != null) {
            val current = tester ?: CustomObjectAnalyzer(
                context,
                listOf(CustomObjectTarget(0L, currentTest.references, currentTest.embeddingThreshold)),
                onResult = { box, progress, _, done -> onTestResult(box, progress, done) },
                onError = onError
            ).also { tester = it }
            current.analyze(image) // closes the image
            return
        }
        tester?.close()
        tester = null
        if (!captureRequested.compareAndSet(true, false)) {
            image.close()
            return
        }
        var source: Bitmap? = null
        var upright: Bitmap? = null
        try {
            ImageEmbedderEngine.ensureOpenCv()
            val frameRotation = image.imageInfo.rotationDegrees
            val bitmap = image.toBitmap().also { source = it }
            val rotated = if (frameRotation == 0) bitmap else Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height,
                Matrix().apply { postRotate(frameRotation.toFloat()) }, true
            )
            upright = rotated
            val box = ReferenceBuilder.guideBox(rotated.width, rotated.height)
            val cropped = Bitmap.createBitmap(rotated, box.x, box.y, box.width, box.height)
            val side = ReferenceBuilder.REFERENCE_MAX_SIDE
            val crop = if (cropped.width > side) Bitmap.createScaledBitmap(cropped, side, side, true)
                .also { cropped.recycle() } else cropped
            val embedding = (embedder ?: ImageEmbedderEngine(context).also { embedder = it }).embed(crop)
            val rgba = Mat()
            val gray = Mat()
            Utils.bitmapToMat(crop, rgba)
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
            rgba.release()
            val built = ReferenceBuilder.build(
                gray, extractor ?: OrbExtractor(OrbExtractor.REFERENCE_FEATURES).also { extractor = it }, embedding
            )
            gray.release()
            val jpeg = ByteArrayOutputStream().use { out ->
                crop.compress(Bitmap.CompressFormat.JPEG, 88, out)
                out.toByteArray()
            }
            onShot(
                Shot(
                    CustomObjectReference(jpeg, built.features),
                    EnrollmentRules.checkShot(built.features, built.sharpness),
                    crop
                )
            )
        } catch (exception: Exception) {
            onError(exception)
        } finally {
            if (upright !== source) upright?.recycle()
            source?.recycle()
            image.close()
        }
    }

    override fun close() {
        tester?.close()
        tester = null
        embedder?.close()
        embedder = null
    }
}

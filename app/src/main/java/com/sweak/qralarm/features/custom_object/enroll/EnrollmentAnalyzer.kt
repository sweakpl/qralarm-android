package com.sweak.qralarm.features.custom_object.enroll

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.sweak.qralarm.core.domain.custom_object.CustomObjectReference
import com.sweak.qralarm.core.recognition.custom.EnrollmentRules
import com.sweak.qralarm.core.recognition.custom.KeypointExtractor
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
    /** Sharp, usable frames picked from a recorded clip, at most one per time slice. */
    private val onClip: (shots: List<Shot>, lowDetailShare: Float) -> Unit,
    private val onTestResult: (ObjectDetectionBox?, Float, Boolean) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer, AutoCloseable {

    class Shot(
        val reference: CustomObjectReference,
        val issue: EnrollmentRules.ShotIssue?,
        val preview: Bitmap,
        val sharpness: Double = 0.0
    )

    private val captureRequested = AtomicBoolean(false)
    private val recordRequested = AtomicBoolean(false)
    private var recordingStartMillis: Long? = null
    private val clipSlices = HashMap<Int, Shot>()
    private var clipFrames = 0
    private var clipLowDetailFrames = 0
    private class Test(val references: List<ReferenceFeatures>, val embeddingThreshold: Float)
    @Volatile private var test: Test? = null
    private var tester: CustomObjectAnalyzer? = null
    private var extractor: KeypointExtractor? = null
    private var embedder: ImageEmbedderEngine? = null

    fun requestCapture() = captureRequested.set(true)

    /** Records for [CLIP_MILLIS] while the user turns the item; see [onClip]. */
    fun requestClip() = recordRequested.set(true)

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
        if (recordRequested.compareAndSet(true, false)) {
            clipSlices.values.forEach { it.preview.recycle() }
            clipSlices.clear()
            clipFrames = 0
            clipLowDetailFrames = 0
            recordingStartMillis = image.imageInfo.timestamp / 1_000_000
        }
        val recordingStart = recordingStartMillis
        if (recordingStart != null) {
            val now = image.imageInfo.timestamp / 1_000_000
            if (now - recordingStart >= CLIP_MILLIS) {
                recordingStartMillis = null
                image.close()
                onClip(
                    clipSlices.toSortedMap().values.toList(),
                    if (clipFrames == 0) 0f else clipLowDetailFrames.toFloat() / clipFrames
                )
                clipSlices.clear()
                return
            }
            val shot = buildShot(image) ?: return
            clipFrames++
            if (shot.issue == EnrollmentRules.ShotIssue.TOO_LITTLE_DETAIL) clipLowDetailFrames++
            if (shot.issue != null) {
                shot.preview.recycle()
                return
            }
            val slice = ((now - recordingStart) * CLIP_SLICES / CLIP_MILLIS).toInt()
            val kept = clipSlices[slice]
            if (kept == null || shot.sharpness > kept.sharpness) {
                kept?.preview?.recycle()
                clipSlices[slice] = shot
            } else {
                shot.preview.recycle()
            }
            return
        }
        if (!captureRequested.compareAndSet(true, false)) {
            image.close()
            return
        }
        buildShot(image)?.let(onShot)
    }

    /** Crops, describes and checks one frame. Always closes [image]. */
    private fun buildShot(image: ImageProxy): Shot? {
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
                gray, extractor ?: KeypointExtractor(KeypointExtractor.REFERENCE_FEATURES).also { extractor = it }, embedding
            )
            gray.release()
            val jpeg = ByteArrayOutputStream().use { out ->
                crop.compress(Bitmap.CompressFormat.JPEG, 88, out)
                out.toByteArray()
            }
            return Shot(
                CustomObjectReference(jpeg, built.features),
                EnrollmentRules.checkShot(built.features, built.sharpness),
                crop,
                built.sharpness
            )
        } catch (exception: Exception) {
            onError(exception)
            return null
        } finally {
            if (upright !== source) upright?.recycle()
            source?.recycle()
            image.close()
        }
    }

    companion object {
        const val CLIP_MILLIS = 5_000L
        const val CLIP_SLICES = 8
    }

    override fun close() {
        clipSlices.values.forEach { it.preview.recycle() }
        clipSlices.clear()
        tester?.close()
        tester = null
        embedder?.close()
        embedder = null
    }
}

package com.sweak.qralarm.core.ui.components.object_scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.sweak.qralarm.core.domain.recognition.ObjectConfirmation
import com.sweak.qralarm.core.recognition.custom.CustomObjectRecognizer
import com.sweak.qralarm.core.recognition.custom.OrbExtractor
import com.sweak.qralarm.core.recognition.custom.ReferenceBuilder
import com.sweak.qralarm.core.recognition.custom.ReferenceFeatures
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * Live recognition of one custom object. Same threading contract as [ObjectAnalyzer]: every
 * method, including close, runs on the camera's single worker.
 */
class CustomObjectAnalyzer(
    private val context: Context,
    private val references: List<ReferenceFeatures>,
    private val embeddingThreshold: Float,
    private val onResult: (ObjectDetectionBox?, Float, Boolean) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer, AutoCloseable {
    private var recognizer: CustomObjectRecognizer? = null
    private var extractor: OrbExtractor? = null
    private var embedder: ImageEmbedderEngine? = null
    private val confirmation = ObjectConfirmation(ObjectConfirmation.CUSTOM_OBJECT_POLICY)
    private var failed = false
    private var rotation: Int? = null

    override fun analyze(image: ImageProxy) {
        var bitmap: Bitmap? = null
        var upright: Bitmap? = null
        var rgba: Mat? = null
        var gray: Mat? = null
        try {
            if (failed) return
            val frameRotation = image.imageInfo.rotationDegrees
            if (rotation != frameRotation) {
                confirmation.reset()
                rotation = frameRotation
            }
            val timestampMillis = image.imageInfo.timestamp / 1_000_000
            val engine = recognizer ?: run {
                ImageEmbedderEngine.ensureOpenCv()
                extractor = OrbExtractor(OrbExtractor.FRAME_FEATURES)
                if (references.any { it.embedding.isNotEmpty() }) embedder = ImageEmbedderEngine(context)
                CustomObjectRecognizer(references, embeddingThreshold).also { recognizer = it }
            }
            val source = image.toBitmap().also { bitmap = it }
            val rotated = if (frameRotation == 0) source else Bitmap.createBitmap(
                source, 0, 0, source.width, source.height,
                Matrix().apply { postRotate(frameRotation.toFloat()) }, true
            )
            upright = rotated
            val embeddings = embedder?.let { e -> frameEmbeddings(e, rotated) }
            val color = Mat().also { rgba = it }
            Utils.bitmapToMat(rotated, color)
            val grayFull = Mat()
            Imgproc.cvtColor(color, grayFull, Imgproc.COLOR_RGBA2GRAY)
            val frame = ReferenceBuilder.resizeToMaxSide(grayFull, ReferenceBuilder.FRAME_MAX_SIDE).also { gray = it }
            grayFull.release()
            val features = requireNotNull(extractor).extract(frame)
            val result = engine.evaluate(features, frame.cols(), frame.rows(), frame, embeddings)
            val box = result.match?.corners?.let { c ->
                val xs = (0 until 4).map { c[it * 2] / frame.cols() }
                val ys = (0 until 4).map { c[it * 2 + 1] / frame.rows() }
                ObjectDetectionBox(
                    xs.min().coerceIn(0f, 1f), ys.min().coerceIn(0f, 1f),
                    xs.max().coerceIn(0f, 1f), ys.max().coerceIn(0f, 1f),
                    rotated.width, rotated.height
                )
            }
            val completed = confirmation.observe(if (result.verified) 1f else null, timestampMillis)
            onResult(box, confirmation.progress, completed)
        } catch (exception: Exception) {
            failed = true
            confirmation.reset()
            onError(exception)
        } finally {
            rgba?.release()
            gray?.release()
            if (upright !== bitmap) upright?.recycle()
            bitmap?.recycle()
            image.close()
        }
    }

    override fun close() {
        confirmation.reset()
        recognizer?.close()
        recognizer = null
        embedder?.close()
        embedder = null
        extractor = null
    }

    companion object {
        /** The whole frame and the centre region where people hold the item. */
        fun frameEmbeddings(embedder: ImageEmbedderEngine, bitmap: Bitmap): List<FloatArray> {
            val side = (minOf(bitmap.width, bitmap.height) * 0.7f).toInt()
            val crop = Bitmap.createBitmap(bitmap, (bitmap.width - side) / 2, (bitmap.height - side) / 2, side, side)
            return try {
                listOf(embedder.embed(bitmap), embedder.embed(crop))
            } finally {
                if (crop !== bitmap) crop.recycle()
            }
        }
    }
}

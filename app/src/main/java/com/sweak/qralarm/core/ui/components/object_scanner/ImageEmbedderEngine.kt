package com.sweak.qralarm.core.ui.components.object_scanner

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import org.opencv.android.OpenCVLoader

/** MobileNetV3-small image embeddings. Not thread safe: create and use on one worker thread. */
class ImageEmbedderEngine(context: Context) : AutoCloseable {
    private val embedder: ImageEmbedder = ImageEmbedder.createFromOptions(
        context,
        ImageEmbedder.ImageEmbedderOptions.builder()
            .setBaseOptions(
                BaseOptions.builder().setModelAssetPath(MODEL_ASSET).setDelegate(Delegate.CPU).build()
            )
            .setRunningMode(RunningMode.IMAGE)
            .setL2Normalize(true)
            .build()
    )

    fun embed(bitmap: Bitmap): FloatArray {
        val image = BitmapImageBuilder(bitmap).build()
        try {
            return embedder.embed(image).embeddingResult().embeddings().first().floatEmbedding()
        } finally {
            image.close()
        }
    }

    override fun close() = embedder.close()

    companion object {
        const val MODEL_ASSET = "mobilenet-v3-small-embedder.tflite"
        const val SCENES_ASSET = "generic-scene-embeddings.bin"

        /** Safe to call repeatedly; loads the bundled OpenCV native library. */
        fun ensureOpenCv() {
            check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        }

        /** Embeddings of bundled generic scenes (rooms, shelves, tables) used to set the gate. */
        fun loadSceneEmbeddings(context: Context): List<FloatArray> =
            context.assets.open(SCENES_ASSET).use { stream ->
                val input = java.io.DataInputStream(stream.buffered())
                val count = input.readInt()
                val dim = input.readInt()
                List(count) { FloatArray(dim) { input.readFloat() } }
            }
    }
}

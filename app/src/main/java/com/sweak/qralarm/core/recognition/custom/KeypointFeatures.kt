package com.sweak.qralarm.core.recognition.custom

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.SIFT

/**
 * Keypoints and SIFT descriptors of one image, as plain arrays. Descriptors are kept as bytes
 * (SIFT values fit in 0..255) and turned into RootSIFT floats for matching.
 */
class KeypointFeatures(val keypoints: FloatArray, val descriptors: ByteArray) {
    val count: Int get() = keypoints.size / 2

    /** RootSIFT: L1-normalise, then square root, so Euclidean matching acts like Hellinger. */
    fun descriptorMat(): Mat {
        val length = ReferenceFeatures.DESCRIPTOR_BYTES
        val values = FloatArray(count * length)
        for (row in 0 until count) {
            var sum = 0f
            for (i in 0 until length) sum += descriptors[row * length + i].toInt() and 0xFF
            val norm = sum + 1e-7f
            for (i in 0 until length) {
                values[row * length + i] =
                    kotlin.math.sqrt((descriptors[row * length + i].toInt() and 0xFF) / norm)
            }
        }
        return Mat(count, length, CvType.CV_32F).also { if (count > 0) it.put(0, 0, values) }
    }
}

/**
 * SIFT keypoints. SIFT was chosen over ORB on the ground-truth sets (eval/RESULTS.md): it keeps
 * matching when a curved label is turned or seen from further away. Not thread safe.
 */
class KeypointExtractor(maxFeatures: Int) : AutoCloseable {
    private val sift: SIFT = SIFT.create(maxFeatures, 3, CONTRAST_THRESHOLD, 10.0, 1.6)

    /** [gray] must be a single-channel 8-bit image. */
    fun extract(gray: Mat): KeypointFeatures {
        val keypointMat = MatOfKeyPoint()
        val descriptorMat = Mat()
        val bytes = Mat()
        try {
            sift.detectAndCompute(gray, Mat(), keypointMat, descriptorMat)
            val keypoints = keypointMat.toArray()
            if (keypoints.isEmpty() || descriptorMat.empty()) return KeypointFeatures(FloatArray(0), ByteArray(0))
            // Saturating conversion; SIFT components are already within 0..255.
            descriptorMat.convertTo(bytes, CvType.CV_8U)
            val descriptors = ByteArray(keypoints.size * ReferenceFeatures.DESCRIPTOR_BYTES)
            bytes.get(0, 0, descriptors)
            val coordinates = FloatArray(keypoints.size * 2)
            keypoints.forEachIndexed { index, keypoint ->
                coordinates[index * 2] = keypoint.pt.x.toFloat()
                coordinates[index * 2 + 1] = keypoint.pt.y.toFloat()
            }
            return KeypointFeatures(coordinates, descriptors)
        } finally {
            keypointMat.release()
            descriptorMat.release()
            bytes.release()
        }
    }

    override fun close() = Unit

    companion object {
        const val CONTRAST_THRESHOLD = 0.02
        const val FRAME_FEATURES = 1000
        const val REFERENCE_FEATURES = 600
    }
}

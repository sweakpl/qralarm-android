package com.sweak.qralarm.core.recognition.custom

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.ORB

/** Keypoints and descriptors of one image, as plain arrays. */
class OrbFeatures(val keypoints: FloatArray, val descriptors: ByteArray) {
    val count: Int get() = keypoints.size / 2

    fun descriptorMat(): Mat = Mat(count, ReferenceFeatures.DESCRIPTOR_BYTES, CvType.CV_8UC1).also {
        if (count > 0) it.put(0, 0, descriptors)
    }
}

/** Not thread safe: use one per worker thread. */
class OrbExtractor(maxFeatures: Int) : AutoCloseable {
    private val orb: ORB = ORB.create(maxFeatures, 1.2f, 8, 31, 0, 2, ORB.HARRIS_SCORE, 31, FAST_THRESHOLD)

    /** [gray] must be a single-channel 8-bit image. */
    fun extract(gray: Mat): OrbFeatures {
        val keypointMat = MatOfKeyPoint()
        val descriptorMat = Mat()
        try {
            orb.detectAndCompute(gray, Mat(), keypointMat, descriptorMat)
            val keypoints = keypointMat.toArray()
            if (keypoints.isEmpty() || descriptorMat.empty()) return OrbFeatures(FloatArray(0), ByteArray(0))
            val descriptors = ByteArray(keypoints.size * ReferenceFeatures.DESCRIPTOR_BYTES)
            descriptorMat.get(0, 0, descriptors)
            val coordinates = FloatArray(keypoints.size * 2)
            keypoints.forEachIndexed { index, keypoint ->
                coordinates[index * 2] = keypoint.pt.x.toFloat()
                coordinates[index * 2 + 1] = keypoint.pt.y.toFloat()
            }
            return OrbFeatures(coordinates, descriptors)
        } finally {
            keypointMat.release()
            descriptorMat.release()
        }
    }

    override fun close() = Unit

    companion object {
        const val FAST_THRESHOLD = 15
        const val FRAME_FEATURES = 1500
        const val REFERENCE_FEATURES = 600
    }
}

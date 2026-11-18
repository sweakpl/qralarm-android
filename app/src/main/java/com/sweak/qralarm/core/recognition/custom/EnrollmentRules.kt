package com.sweak.qralarm.core.recognition.custom

import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.imgproc.Imgproc

/** What a set of reference photos has to satisfy before an object can be saved. */
object EnrollmentRules {
    const val MIN_REFERENCES = 3
    const val MAX_REFERENCES = 5
    const val MIN_KEYPOINTS = 150
    const val MIN_SHARPNESS = 60.0

    /** Guide box: centred square, this share of the shorter image side. */
    const val GUIDE_BOX_FRACTION = 0.7f

    /** Embedding gate bounds; the threshold sits just above what generic scenes reach. */
    const val GATE_MARGIN = 0.02f
    const val GATE_MIN = 0.20f
    const val GATE_MAX = 0.60f

    /** Consistency between two photos of the same item from different angles is a looser test. */
    val CONSISTENCY_PARAMS = GeometricVerifier.Params(minInliers = 12, minInlierRatio = 0.2, minInlierSpread = 0.05)

    enum class ShotIssue { BLURRY, TOO_LITTLE_DETAIL }

    fun checkShot(features: ReferenceFeatures, sharpness: Double): ShotIssue? = when {
        sharpness < MIN_SHARPNESS -> ShotIssue.BLURRY
        features.keypointCount < MIN_KEYPOINTS -> ShotIssue.TOO_LITTLE_DETAIL
        else -> null
    }

    /** Variance of the Laplacian: low for blurry images. */
    fun sharpness(gray: Mat): Double {
        val laplacian = Mat()
        val mean = MatOfDouble()
        val deviation = MatOfDouble()
        try {
            Imgproc.Laplacian(gray, laplacian, CvType.CV_64F)
            Core.meanStdDev(laplacian, mean, deviation)
            val sd = deviation.toArray().firstOrNull() ?: 0.0
            return sd * sd
        } finally {
            laplacian.release()
            mean.release()
            deviation.release()
        }
    }

    /** Indices of photos that do not match any other photo of the set. */
    fun inconsistentReferences(references: List<ReferenceFeatures>): List<Int> {
        if (references.size < 2) return emptyList()
        return references.indices.filter { index ->
            val others = references.filterIndexed { i, _ -> i != index }
            GeometricVerifier(others, CONSISTENCY_PARAMS).use { verifier ->
                val self = references[index]
                verifier.verify(OrbFeatures(self.keypoints, self.descriptors), self.width, self.height) == null
            }
        }
    }

    /**
     * [genericSimilarities]: for each reference, its similarity to every bundled generic scene.
     * The gate has to reject almost all of them.
     */
    fun embeddingThreshold(genericSimilarities: List<FloatArray>): Float {
        val p95 = genericSimilarities.filter { it.isNotEmpty() }.maxOfOrNull { similarities ->
            val sorted = similarities.sorted()
            sorted[((sorted.size - 1) * 0.95).toInt()]
        } ?: return GATE_MIN
        return (p95 + GATE_MARGIN).coerceIn(GATE_MIN, GATE_MAX)
    }
}
